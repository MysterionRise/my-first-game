import com.jme3.renderer.{ViewPort, RenderManager}
import com.jme3.scene.Spatial
import com.jme3.scene.control.AbstractControl

class SeekerControl(spatial: Spatial) extends AbstractControl {
  override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}

  override def controlUpdate(tpf: Float): Unit = {}
}
