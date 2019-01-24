package br.org.sidia.eva;

import android.opengl.GLES30;

import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRShaderId;
import br.org.sidia.eva.shaders.SXRPointCloudShader;
import com.samsungxr.mixedreality.IMixedReality;
import com.samsungxr.mixedreality.IMixedRealityEvents;
import com.samsungxr.mixedreality.SXRPointCloud;

public class PointCloudHandler implements IMixedRealityEvents {
    IMixedReality mMixedReality;
    SXRPointCloud mOldPointCloud;
    SXRNode mPointCloudNode;
    EvaContext mEvaContext;

    public PointCloudHandler(EvaContext evaContext) {
        mEvaContext = evaContext;

        SXRMaterial mat = new SXRMaterial(evaContext.getSXRContext(),
                new SXRShaderId(SXRPointCloudShader.class));
        mat.setVec3("u_color", 0.94f,0.61f,1f);

        SXRRenderData renderData = new SXRRenderData(evaContext.getSXRContext());
        renderData.setDrawMode(GLES30.GL_POINTS);
        renderData.setMaterial(mat);

        mPointCloudNode = new SXRNode(evaContext.getSXRContext());
        mPointCloudNode.attachComponent(renderData);
    }

    public void addOnScene() {
        mEvaContext.getMainScene().addNode(mPointCloudNode);
    }

    public void removeFromScene() {
        mEvaContext.getMainScene().removeNode(mPointCloudNode);
    }

    @Override
    public void onMixedRealityStart(IMixedReality mixedReality) {
        mMixedReality = mixedReality;
        addOnScene();
    }

    @Override
    public void onMixedRealityStop(IMixedReality iMixedReality) {

    }

    @Override
    public void onMixedRealityUpdate(IMixedReality iMixedReality) {
        SXRPointCloud newPointCloud = mMixedReality.acquirePointCloud();
        if (mOldPointCloud != newPointCloud) {
            float[] cloudPoints = newPointCloud.getPoints();
            if (cloudPoints.length == 0) {
                return;
            }

            SXRMesh mesh = new SXRMesh(mEvaContext.getSXRContext());
            mesh.setVertices(cloudPoints);
            mPointCloudNode.getRenderData().setMesh(mesh);

            mOldPointCloud = newPointCloud;
            newPointCloud.release();
        }
    }
}
