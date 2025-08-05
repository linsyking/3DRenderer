use glam::{Vec2, Vec3};
use nalgebra::{DMatrix, DVector};
use std::f32::consts::PI;

#[derive(Debug, Clone)]
pub struct Mesh {
    pub positions: Vec<Vec3>,
    pub normals: Vec<Vec3>,
    pub uvs: Vec<Vec2>,
    pub triangles: Vec<[u32; 3]>,
}

type PointSet = Vec<Vec3>;
type CubicBezierCurve = Vec<Vec3>;

fn cox_de_boor(i: usize, k: usize, t: f32, knots: &[f32]) -> f32 {
    if k == 0 {
        if i < knots.len() - 1 && knots[i] <= t && t < knots[i + 1] { 1.0 }
        else if i == knots.len() - 2 && knots[i] <= t && t <= knots[i + 1] { 1.0 }
        else { 0.0 }
    } else {
        let mut left = 0.0;
        let mut right = 0.0;

        if i + k < knots.len() {
            let left_den = knots[i + k] - knots[i];
            if left_den != 0.0 {
                left = (t - knots[i]) / left_den * cox_de_boor(i, k - 1, t, knots);
            }
        }

        if i + k + 1 < knots.len() {
            let right_den = knots[i + k + 1] - knots[i + 1];
            if right_den != 0.0 {
                right = (knots[i + k + 1] - t) / right_den * cox_de_boor(i + 1, k - 1, t, knots);
            }
        }

        left + right
    }
}

/// Fixed version of eval_bspline with proper parameter handling
fn eval_bspline_fixed(control_points: &[Vec3], t: f32) -> Vec3 {
    let n_ctrl_pts = control_points.len();
    let degree = 3;

    if n_ctrl_pts < degree + 1 {
        return control_points[0]; // fallback
    }

    // Parameter range is [0, n_ctrl_pts - degree]
    let max_t = (n_ctrl_pts - degree) as f32;
    let t = t.clamp(0.0, max_t);

    // Open uniform knot vector
    let mut knots = Vec::new();
    for _ in 0..=degree { knots.push(0.0); }
    for j in 1..=(n_ctrl_pts - degree - 1) { knots.push(j as f32); }
    for _ in 0..=degree { knots.push(max_t); }

    // Find the knot span
    let mut span = degree;
    if t >= max_t {
        span = n_ctrl_pts - 1;
    } else {
        while span < n_ctrl_pts && t >= knots[span + 1] {
            span += 1;
        }
    }

    // Evaluate using basis functions
    let mut point = Vec3::ZERO;
    for i in (span - degree)..=span {
        if i < control_points.len() {
            let basis = cox_de_boor(i, degree, t, &knots);
            point += control_points[i] * basis;
        }
    }

    point
}

/// Compute tangent vector for the B-spline curve at parameter t
fn eval_bspline_tangent(control_points: &[Vec3], t: f32) -> Vec3 {
    let epsilon = 1e-4;
    let n_ctrl_pts = control_points.len();
    let degree = 3;
    let max_t = (n_ctrl_pts - degree) as f32;

    let t1 = (t - epsilon).max(0.0);
    let t2 = (t + epsilon).min(max_t);

    let p1 = eval_bspline_fixed(control_points, t1);
    let p2 = eval_bspline_fixed(control_points, t2);

    (p2 - p1).normalize()
}

/// Convert B-spline curve to mesh using camera-aware tube generation
pub fn meshify(curve: &CubicBezierCurve, camera_pos: Vec3, radius: f32) -> Mesh {
    if curve.len() < 2 {
        return Mesh {
            positions: Vec::new(),
            normals: Vec::new(),
            uvs: Vec::new(),
            triangles: Vec::new(),
        };
    }

    const RADIAL_SEGMENTS: usize = 8;

    let segments = curve.len();
    let mut positions = Vec::new();
    let mut normals = Vec::new();
    let mut uvs = Vec::new();
    let mut triangles = Vec::new();

    // Generate vertices
    for (i, &point) in curve.iter().enumerate() {
        // Calculate tangent
        let tangent = if i == 0 {
            (curve[1] - curve[0]).normalize()
        } else if i == curve.len() - 1 {
            (curve[i] - curve[i - 1]).normalize()
        } else {
            ((curve[i + 1] - curve[i - 1]) * 0.5).normalize()
        };

        // Calculate view direction from curve point to camera
        let view_dir = (camera_pos - point).normalize();

        // Create coordinate system for tube cross-section
        let up = if tangent.dot(view_dir).abs() < 0.9 {
            tangent.cross(view_dir).normalize()
        } else {
            // Use world up if tangent is too aligned with view direction
            tangent.cross(Vec3::Y).normalize()
        };
        let right = up.cross(tangent).normalize();

        // Generate points around the circumference
        for j in 0..RADIAL_SEGMENTS {
            let angle = 2.0 * PI * j as f32 / RADIAL_SEGMENTS as f32;
            let cos_angle = angle.cos();
            let sin_angle = angle.sin();

            // Position on tube surface
            let offset = (right * cos_angle + up * sin_angle) * radius;
            let vertex_pos = point + offset;

            // Normal points outward
            let normal = offset.normalize();

            // UV coordinates
            let u = i as f32 / (segments - 1) as f32;
            let v = j as f32 / RADIAL_SEGMENTS as f32;

            positions.push(vertex_pos);
            normals.push(normal);
            uvs.push(Vec2::new(u, v));
        }
    }

    // Generate triangles
    for i in 0..(segments - 1) {
        for j in 0..RADIAL_SEGMENTS {
            let current = (i * RADIAL_SEGMENTS + j) as u32;
            let next_ring = ((i + 1) * RADIAL_SEGMENTS + j) as u32;
            let current_next = (i * RADIAL_SEGMENTS + (j + 1) % RADIAL_SEGMENTS) as u32;
            let next_ring_next = ((i + 1) * RADIAL_SEGMENTS + (j + 1) % RADIAL_SEGMENTS) as u32;

            // Two triangles per quad
            triangles.push([current, next_ring, current_next]);
            triangles.push([current_next, next_ring, next_ring_next]);
        }
    }

    Mesh {
        positions,
        normals,
        uvs,
        triangles,
    }
}

pub fn curvify(point_set: &PointSet) -> CubicBezierCurve {
    if point_set.len() < 4 {
        return point_set.clone();
    }

    // Use your existing control point fitting
    let control_points = fit_bspline_control_points(point_set);

    // Sample using fixed evaluation
    let samples = control_points.len() * 20;
    let degree = 3;
    let n_ctrl_pts = control_points.len();
    let max_t = (n_ctrl_pts - degree) as f32;

    let mut result = Vec::with_capacity(samples);
    for i in 0..samples {
        let t = (max_t * i as f32) / (samples - 1) as f32;
        result.push(eval_bspline_fixed(&control_points, t));
    }

    result
}

// Include your existing functions (they look correct)
fn cubic_bspline_basis(i: usize, t: f32, n_ctrl_pts: usize, u: &[f32]) -> f32 {
    let degree = 3;
    let m = n_ctrl_pts + degree + 1;

    let mut knots = Vec::with_capacity(m);
    for _ in 0..degree+1 { knots.push(0.0); }
    for j in 1..=(n_ctrl_pts - degree - 1) { knots.push(j as f32); }
    for _ in 0..degree+1 { knots.push((n_ctrl_pts - degree) as f32); }

    cox_de_boor(i, degree, t, &knots)
}

fn chord_length_param(points: &PointSet, max_val: f32) -> Vec<f32> {
    let mut u = vec![0.0f32; points.len()];
    for i in 1..points.len() {
        u[i] = u[i-1] + (points[i] - points[i-1]).length();
    }
    let total_length = *u.last().unwrap();
    if total_length == 0.0 {
        return u;
    }
    for val in &mut u {
        *val = *val / total_length * max_val;
    }
    u
}

fn determine_control_point_count(n_points: usize) -> usize {
    std::cmp::max(4, n_points / 10)
}

fn fit_bspline_control_points(points: &PointSet) -> Vec<Vec3> {
    let n_points = points.len();
    if n_points < 4 {
        return points.clone();
    }

    let n_ctrl_pts = determine_control_point_count(n_points);
    let degree = 3;

    let max_knot = (n_ctrl_pts - degree) as f32;
    let u = chord_length_param(points, max_knot);

    let mut data = Vec::with_capacity(n_points * n_ctrl_pts);
    for &param in &u {
        for i in 0..n_ctrl_pts {
            let val = cubic_bspline_basis(i, param, n_ctrl_pts, &[]);
            data.push(val);
        }
    }

    let n = DMatrix::from_row_slice(n_points, n_ctrl_pts, &data);

    let mut qx = DVector::from_element(n_points, 0.0);
    let mut qy = DVector::from_element(n_points, 0.0);
    let mut qz = DVector::from_element(n_points, 0.0);

    for i in 0..n_points {
        qx[i] = points[i].x;
        qy[i] = points[i].y;
        qz[i] = points[i].z;
    }

    let nt = n.transpose();
    let nt_n = &nt * &n;
    let nt_n_inv = nt_n.clone().try_inverse().expect("Matrix inversion failed");

    let p_x = &nt_n_inv * &nt * qx;
    let p_y = &nt_n_inv * &nt * qy;
    let p_z = &nt_n_inv * &nt * qz;

    let mut control_points = Vec::with_capacity(n_ctrl_pts);
    for i in 0..n_ctrl_pts {
        control_points.push(Vec3::new(p_x[i], p_y[i], p_z[i]));
    }
    control_points
}

#[derive(Debug, Clone, Copy)]
pub enum Axis {
    X,
    Y,
    Z,
}

impl Mesh {
    /// Uniform translation - move entire mesh by offset
    pub fn translate(&mut self, offset: Vec3) {
        for pos in &mut self.positions {
            *pos += offset;
        }
        // Normals don't change with translation
    }

    /// Move a single vertex by offset
    pub fn translate_vertex(&mut self, vertex_index: usize, offset: Vec3) {
        if vertex_index < self.positions.len() {
            self.positions[vertex_index] += offset;
            self.recalculate_normals();
        }
    }

    /// Rotate around specified axis by angle in radians
    pub fn rotate(&mut self, axis: Axis, angle: f32) {
        let cos_a = angle.cos();
        let sin_a = angle.sin();

        match axis {
            Axis::X => { // X-axis rotation
                for pos in &mut self.positions {
                    let y = pos.y * cos_a - pos.z * sin_a;
                    let z = pos.y * sin_a + pos.z * cos_a;
                    pos.y = y;
                    pos.z = z;
                }

                for normal in &mut self.normals {
                    let y = normal.y * cos_a - normal.z * sin_a;
                    let z = normal.y * sin_a + normal.z * cos_a;
                    normal.y = y;
                    normal.z = z;
                }
            },
            Axis::Y => { // Y-axis rotation
                for pos in &mut self.positions {
                    let x = pos.x * cos_a + pos.z * sin_a;
                    let z = -pos.x * sin_a + pos.z * cos_a;
                    pos.x = x;
                    pos.z = z;
                }

                for normal in &mut self.normals {
                    let x = normal.x * cos_a + normal.z * sin_a;
                    let z = -normal.x * sin_a + normal.z * cos_a;
                    normal.x = x;
                    normal.z = z;
                }
            },
            Axis::Z => { // Z-axis rotation
                for pos in &mut self.positions {
                    let x = pos.x * cos_a - pos.y * sin_a;
                    let y = pos.x * sin_a + pos.y * cos_a;
                    pos.x = x;
                    pos.y = y;
                }

                for normal in &mut self.normals {
                    let x = normal.x * cos_a - normal.y * sin_a;
                    let y = normal.x * sin_a + normal.y * cos_a;
                    normal.x = x;
                    normal.y = y;
                }
            },
        }
    }

    /// Uniform scaling by a single factor
    pub fn scale(&mut self, factor: f32) {
        for pos in &mut self.positions {
            *pos *= factor;
        }
        // Normals don't need to change for uniform scaling
    }

    /// Non-uniform scaling along each axis
    pub fn scale_xyz(&mut self, scale_x: f32, scale_y: f32, scale_z: f32) {
        for pos in &mut self.positions {
            pos.x *= scale_x;
            pos.y *= scale_y;
            pos.z *= scale_z;
        }
        // Non-uniform scaling affects normals
        self.recalculate_normals();
    }

    /// Reflect across specified axis (flip coordinates)
    pub fn reflect(&mut self, axis: Axis) {
        match axis {
            Axis::X => { // Reflect across X-axis
                for pos in &mut self.positions {
                    pos.x = -pos.x;
                }
            },
            Axis::Y => { // Reflect across Y-axis
                for pos in &mut self.positions {
                    pos.y = -pos.y;
                }
            },
            Axis::Z => { // Reflect across Z-axis
                for pos in &mut self.positions {
                    pos.z = -pos.z;
                }
            },
        }

        self.flip_winding_order();
        self.recalculate_normals();
    }

    /// Subdivide each triangle into 4 smaller triangles (Loop subdivision)
    pub fn subdivide(&mut self) {
        let mut edge_to_midpoint = std::collections::HashMap::new();
        let mut new_triangles = Vec::new();

        // Helper to get or create midpoint vertex
        let mut get_midpoint = |edge: (u32, u32),
                                positions: &mut Vec<Vec3>,
                                normals: &mut Vec<Vec3>,
                                uvs: &mut Vec<Vec2>| -> u32 {
            let ordered_edge = if edge.0 < edge.1 { edge } else { (edge.1, edge.0) };

            if let Some(&midpoint_idx) = edge_to_midpoint.get(&ordered_edge) {
                return midpoint_idx;
            }

            let v0_idx = edge.0 as usize;
            let v1_idx = edge.1 as usize;

            let midpoint_pos = (positions[v0_idx] + positions[v1_idx]) * 0.5;
            let midpoint_normal = (normals[v0_idx] + normals[v1_idx]).normalize();
            let midpoint_uv = (uvs[v0_idx] + uvs[v1_idx]) * 0.5;

            let new_idx = positions.len() as u32;
            positions.push(midpoint_pos);
            normals.push(midpoint_normal);
            uvs.push(midpoint_uv);

            edge_to_midpoint.insert(ordered_edge, new_idx);
            new_idx
        };

        // Process each triangle
        for triangle in &self.triangles {
            let v0 = triangle[0];
            let v1 = triangle[1];
            let v2 = triangle[2];

            // Get midpoints for each edge
            let m01 = get_midpoint((v0, v1), &mut self.positions, &mut self.normals, &mut self.uvs);
            let m12 = get_midpoint((v1, v2), &mut self.positions, &mut self.normals, &mut self.uvs);
            let m20 = get_midpoint((v2, v0), &mut self.positions, &mut self.normals, &mut self.uvs);

            // Create 4 new triangles
            new_triangles.push([v0, m01, m20]);    // Corner triangle 0
            new_triangles.push([v1, m12, m01]);    // Corner triangle 1
            new_triangles.push([v2, m20, m12]);    // Corner triangle 2
            new_triangles.push([m01, m12, m20]);   // Center triangle
        }

        self.triangles = new_triangles;
        self.recalculate_normals();
    }

    /// Recalculate normals based on triangle geometry
    fn recalculate_normals(&mut self) {
        // Reset all normals to zero
        self.normals.fill(Vec3::ZERO);

        // Accumulate face normals for each vertex
        for triangle in &self.triangles {
            let v0 = self.positions[triangle[0] as usize];
            let v1 = self.positions[triangle[1] as usize];
            let v2 = self.positions[triangle[2] as usize];

            let face_normal = (v1 - v0).cross(v2 - v0);
            let face_normal = if face_normal.length() > 0.0 {
                face_normal.normalize()
            } else {
                Vec3::Y // fallback
            };

            self.normals[triangle[0] as usize] += face_normal;
            self.normals[triangle[1] as usize] += face_normal;
            self.normals[triangle[2] as usize] += face_normal;
        }

        // Normalize all accumulated normals
        for normal in &mut self.normals {
            *normal = if normal.length() > 0.0 {
                normal.normalize()
            } else {
                Vec3::Y // fallback for degenerate cases
            };
        }
    }

    /// Flip the winding order of all triangles (used after reflection)
    fn flip_winding_order(&mut self) {
        for triangle in &mut self.triangles {
            triangle.swap(0, 2);
        }
    }
}
