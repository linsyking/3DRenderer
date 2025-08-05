use std::fs::File;
use std::io::{Write, BufWriter};
use crate::geometry::Mesh;
use std::path::Path;
use glam::{Vec2, Vec3};
use tobj::{self, LoadOptions};

pub fn export_obj(mesh: &Mesh, path: &Path) -> std::io::Result<()> {
    let file = File::create(path)?;
    let mut writer = BufWriter::new(file);

    for p in &mesh.positions {
        writeln!(writer, "v {} {} {}", p.x, p.y, p.z)?;
    }

    for uv in &mesh.uvs {
        writeln!(writer, "vt {} {}", uv.x, uv.y)?;
    }

    for n in &mesh.normals {
        writeln!(writer, "vn {} {} {}", n.x, n.y, n.z)?;
    }

    let has_uv = !mesh.uvs.is_empty();
    let has_normals = !mesh.normals.is_empty();

    for face in &mesh.triangles {
        let f = |i: u32| {
            let vi = i + 1; // OBJ is 1-based
            match (has_uv, has_normals) {
                (true, true) => format!("{0}/{0}/{0}", vi),
                (true, false) => format!("{0}/{0}", vi),
                (false, true) => format!("{0}//{0}", vi),
                (false, false) => format!("{}", vi),
            }
        };

        writeln!(writer, "f {} {} {}", f(face[0]), f(face[1]), f(face[2]))?;
    }

    Ok(())
}

pub fn load_obj(path: &Path) -> Result<Mesh, String> {
    let (models, _) = tobj::load_obj(
        path,
        &LoadOptions {
            triangulate: true,
            single_index: false,
            ..Default::default()
        },
    ).map_err(|e| format!("Failed to load OBJ: {}", e))?;

    if models.is_empty() {
        return Err("OBJ file contains no geometry.".to_string());
    }

    let mesh_data = &models[0].mesh;

    // Convert positions
    let mut positions = Vec::new();
    for p in mesh_data.positions.chunks(3) {
        positions.push(Vec3::new(p[0], p[1], p[2]));
    }

    // Convert normals (if present)
    let mut normals = Vec::new();
    if !mesh_data.normals.is_empty() {
        for n in mesh_data.normals.chunks(3) {
            normals.push(Vec3::new(n[0], n[1], n[2]));
        }
    }

    // Convert texture coordinates (if present)
    let mut uvs = Vec::new();
    if !mesh_data.texcoords.is_empty() {
        for uv in mesh_data.texcoords.chunks(2) {
            uvs.push(Vec2::new(uv[0], uv[1]));
        }
    }

    // Convert triangle indices
    let mut triangles = Vec::new();
    for tri in mesh_data.indices.chunks(3) {
        if tri.len() == 3 {
            triangles.push([tri[0], tri[1], tri[2]]);
        }
    }

    Ok(Mesh {
        positions,
        normals,
        uvs,
        triangles,
    })
}
