use bevy::core_pipeline::prepass::NormalPrepass;
use glam::{Vec3};

pub struct Mesh {
    pub positions: Vec<Vec3>,
    pub normals: Vec<Vec3>,
    pub uvs: Vec<Vec2>,
    pub triangles: Vec<[u32; 3]>,
}

pub struct PointSet{
    pub points: Vec<Vec3>,
}

pub struct BezierCurve {
    pub control_points: Vec<Vec3>,
}

fn curvify(point_set: &PointSet) -> BezierCurve {
    unimplemented!("Convert PointSet to BezierCurve");
}

fn meshify(curve: &BezierCurve) -> Mesh {
    unimplemented!("Convert BezierCurve to Mesh");
}

