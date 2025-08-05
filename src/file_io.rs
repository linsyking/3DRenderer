use crate::geometry::Mesh as GMesh;
use bevy::asset::RenderAssetUsages;
use glam::{Vec2, Vec3};
use std::fmt::Write;
use tobj::{self, LoadOptions};

use bevy::render::mesh::{Indices, Mesh, VertexAttributeValues};
use bevy::render::render_resource::PrimitiveTopology;

pub fn to_bevy_mesh(mesh: &GMesh) -> Mesh {
    let mut bevy_mesh = Mesh::new(
        PrimitiveTopology::TriangleList,
        RenderAssetUsages::MAIN_WORLD,
    );

    // Convert positions to Vec<[f32; 3]>
    let positions: Vec<[f32; 3]> = mesh.positions.iter().map(|v| [v.x, v.y, v.z]).collect();

    // Normals are optional
    let normals: Option<Vec<[f32; 3]>> = if !mesh.normals.is_empty() {
        Some(mesh.normals.iter().map(|n| [n.x, n.y, n.z]).collect())
    } else {
        None
    };

    // UVs are optional
    let uvs: Option<Vec<[f32; 2]>> = if !mesh.uvs.is_empty() {
        Some(mesh.uvs.iter().map(|uv| [uv.x, uv.y]).collect())
    } else {
        None
    };

    // Set vertex attributes
    bevy_mesh.insert_attribute(
        Mesh::ATTRIBUTE_POSITION,
        VertexAttributeValues::from(positions),
    );
    if let Some(n) = normals {
        bevy_mesh.insert_attribute(Mesh::ATTRIBUTE_NORMAL, VertexAttributeValues::from(n));
    }
    if let Some(t) = uvs {
        bevy_mesh.insert_attribute(Mesh::ATTRIBUTE_UV_0, VertexAttributeValues::from(t));
    }

    // Set indices
    let indices: Vec<u32> = mesh.triangles.iter().flat_map(|tri| tri.to_vec()).collect();
    bevy_mesh.insert_indices(Indices::U32(indices));

    bevy_mesh
}

pub fn export_obj_to_string(mesh: &GMesh) -> String {
    let mut output = String::new();

    for p in &mesh.positions {
        writeln!(output, "v {} {} {}", p.x, p.y, p.z).unwrap();
    }

    for uv in &mesh.uvs {
        writeln!(output, "vt {} {}", uv.x, uv.y).unwrap();
    }

    for n in &mesh.normals {
        writeln!(output, "vn {} {} {}", n.x, n.y, n.z).unwrap();
    }

    let has_uv = !mesh.uvs.is_empty();
    let has_normals = !mesh.normals.is_empty();

    for face in &mesh.triangles {
        let f = |i: u32| {
            let vi = i + 1; // OBJ indices are 1-based
            match (has_uv, has_normals) {
                (true, true) => format!("{0}/{0}/{0}", vi),
                (true, false) => format!("{0}/{0}", vi),
                (false, true) => format!("{0}//{0}", vi),
                (false, false) => format!("{}", vi),
            }
        };

        writeln!(output, "f {} {} {}", f(face[0]), f(face[1]), f(face[2])).unwrap();
    }

    output
}

pub fn load_obj(data: String) -> Result<GMesh, String> {
    let (models, _) = tobj::load_obj_buf(
        &mut data.as_bytes(),
        &LoadOptions {
            triangulate: true,
            single_index: false,
            ..Default::default()
        },
        |_| Ok((Vec::new(), std::collections::HashMap::new())),
    )
    .map_err(|e| format!("Failed to load OBJ: {}", e))?;

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

    Ok(GMesh {
        positions,
        normals,
        uvs,
        triangles,
    })
}
