package fr.utln.airhockey;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

public class CustomQuad extends Mesh {

    public CustomQuad(boolean frontFace, boolean backFace, boolean leftFace, boolean rightFace, boolean topFace, boolean bottomFace) {
        // Les positions des sommets
        float[] vertices = new float[]{
                -2f, -1.5f, 5f, // 0 front
                2f, -1.5f, 5f,  // 1
                2f, 1.5f, 5f,   // 2
                -2f, 1.5f, 5f,  // 3
                -2f, -1.5f, -5f,// 4 back
                2f, -1.5f, -5f, // 5
                2f, 1.5f, -5f,  // 6
                -2f, 1.5f, -5f, // 7
                -2f, -1.5f, 5f, // 8 left
                -2f, -1.5f, -5f,// 9
                -2f, 1.5f, -5f, // 10
                -2f, 1.5f, 5f,  // 11
                2f, -1.5f, 5f,  // 12 right
                2f, -1.5f, -5f, // 13
                2f, 1.5f, -5f,  // 14
                2f, 1.5f, 5f,   // 15
                -2f, 1.5f, 5f,  // 16 top
                2f, 1.5f, 5f,   // 17
                2f, 1.5f, -5f,  // 18
                -2f, 1.5f, -5f, // 19
                -2f, -1.5f, 5f, // 20 bottom
                2f, -1.5f, 5f,  // 21
                2f, -1.5f, -5f, // 22
                -2f, -1.5f, -5f // 23
        };

        // Les coordonnées de texture
        float[] texCoord = new float[]{
                0, 0, // 0
                1, 0, // 1
                1, 1, // 2
                0, 1, // 3
                0, 0, // 4
                1, 0, // 5
                1, 1, // 6
                0, 1, // 7
                0, 0, // 8
                1, 0, // 9
                1, 1, // 10
                0, 1, // 11
                0, 0, // 12
                1, 0, // 13
                1, 1, // 14
                0, 1, // 15
                0, 0, // 16
                1, 0, // 17
                1, 1, // 18
                0, 1, // 19
                0, 0, // 20
                1, 0, // 21
                1, 1, // 22
                0, 1  // 23
        };

        // Les indices pour les faces
        short[] indices = new short[36];
        int i = 0;

        if (frontFace) {
            indices[i++] = 0; indices[i++] = 1; indices[i++] = 2;
            indices[i++] = 0; indices[i++] = 2; indices[i++] = 3;
        }
        if (backFace) {
            indices[i++] = 4; indices[i++] = 6; indices[i++] = 5;
            indices[i++] = 4; indices[i++] = 7; indices[i++] = 6;
        }
        if (leftFace) {
            indices[i++] = 8; indices[i++] = 9; indices[i++] = 10;
            indices[i++] = 8; indices[i++] = 10; indices[i++] = 11;
        }
        if (rightFace) {
            indices[i++] = 12; indices[i++] = 13; indices[i++] = 14;
            indices[i++] = 12; indices[i++] = 14; indices[i++] = 15;
        }
        if (topFace) {
            indices[i++] = 16; indices[i++] = 17; indices[i++] = 18;
            indices[i++] = 16; indices[i++] = 18; indices[i++] = 19;
        }
        if (bottomFace) {
            indices[i++] = 20; indices[i++] = 21; indices[i++] = 22;
            indices[i++] = 20; indices[i++] = 22; indices[i++] = 23;
        }

        // Définir les buffers
        setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
        setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createShortBuffer(indices));
        updateBound();
    }
}