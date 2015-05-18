package org.mystic

import com.jme3.app.SimpleApplication
import com.jme3.input.controls.{ActionListener, AnalogListener, KeyTrigger, MouseButtonTrigger}
import com.jme3.input.{KeyInput, MouseInput}
import com.jme3.material.Material
import com.jme3.material.RenderState.BlendMode
import com.jme3.math.{FastMath, Vector2f, Vector3f}
import com.jme3.scene.{Node, Spatial}
import com.jme3.texture.Texture2D
import com.jme3.ui.Picture
import org.mystic.controls.{BulletControl, PlayerControl, SeekerControl, WandererControl}

import scala.util.Random

object MyFirstGame extends SimpleApplication with ActionListener with AnalogListener {

  private var player: Spatial = _
  private val Alive = "alive"
  private val Left = "left"
  private val Right = "right"
  private val Up = "up"
  private val Down = "down"
  private val MouseClick = "mouseClick"

  private var bulletCooldown: Long = 0
  private var bulletNode: Node = _
  private var enemyNode: Node = _

  private var enemySpawnCooldown: Long = _
  private var enemySpawnChance: Float = 80

  override def simpleInitApp(): Unit = {
    // set up camera for 2D
    cam.setParallelProjection(true)
    cam.setLocation(new Vector3f(0, 0, 0.5f))
    getFlyByCamera().setEnabled(false)

    // turn off stats view
    setDisplayStatView(false)
    setDisplayFps(false)

    // add listener for keyboard
    inputManager.addMapping(Left, new KeyTrigger(KeyInput.KEY_LEFT))
    inputManager.addMapping(Right, new KeyTrigger(KeyInput.KEY_RIGHT))
    inputManager.addMapping(Up, new KeyTrigger(KeyInput.KEY_UP))
    inputManager.addMapping(Down, new KeyTrigger(KeyInput.KEY_DOWN))
    inputManager.addListener(this, Left)
    inputManager.addListener(this, Right)
    inputManager.addListener(this, Up)
    inputManager.addListener(this, Down)

    // add listener for mouse
    inputManager.addMapping(MouseClick, new MouseButtonTrigger(MouseInput.BUTTON_LEFT))
    inputManager.addListener(this, MouseClick)

    //        setup the bulletNode
    bulletNode = new Node("bullets")
    guiNode.attachChild(bulletNode)

    // set up the enemyNode
    enemyNode = new Node("enemies")
    guiNode.attachChild(enemyNode)

    // add player
    player = getSpatial("Player")
    player.setUserData(Alive, true)
    player.move(settings.getWidth() / 2, settings.getHeight() / 2, 0)
    guiNode.attachChild(player)
    player.addControl(new PlayerControl(settings.getWidth(), settings.getHeight()))
  }

  // entry point of the game
  def main(args: Array[String]): Unit = {
    MyFirstGame.start()
  }

  private def getSpatial(name: String): Spatial = {
    val node = new Node(name)
    // load picture
    val pic = new Picture(name)
    val tex: Texture2D = assetManager.loadTexture(s"Textures/${name}.png").asInstanceOf[Texture2D]
    pic.setTexture(assetManager, tex, true)

    // adjust picture
    val width = tex.getImage.getWidth
    val height = tex.getImage.getHeight
    pic.setWidth(width)
    pic.setHeight(height)
    // for rotation purposes
    pic.move(-width / 2f, -height / 2f, 0)

    // add a material to the picture
    val pictureMaterial = new Material(assetManager, "Common/MatDefs/Gui/Gui.j3md")
    pictureMaterial.getAdditionalRenderState.setBlendMode(BlendMode.AlphaAdditive)
    node.setMaterial(pictureMaterial)

    // set the radius of the spatial
    // (using width only as a simple approximation)
    node.setUserData("radius", width / 2)

    // attach the picture to the node and return it
    node.attachChild(pic)
    node
  }

  override def onAction(name: String, isPressed: Boolean, tpf: Float): Unit = {
    if (player.getUserData[Boolean](Alive)) {
      name match {
        // TODO change it to getControl by class
        case Up => player.getControl(0).asInstanceOf[PlayerControl].up = isPressed
        case Down => player.getControl(0).asInstanceOf[PlayerControl].down = isPressed
        case Left => player.getControl(0).asInstanceOf[PlayerControl].left = isPressed
        case Right => player.getControl(0).asInstanceOf[PlayerControl].right = isPressed
        case _ =>
      }

    }
  }

  def getAimDirection: Vector3f = {
    val mouse = inputManager.getCursorPosition
    val playerPosition = player.getLocalTranslation
    val difference = new Vector3f(mouse.x - playerPosition.x, mouse.y - playerPosition.y, 0)
    difference.normalizeLocal
  }

  def launchTwoBullets = {
    if (System.currentTimeMillis() - bulletCooldown > 83f) {
      bulletCooldown = System.currentTimeMillis
      val aim = getAimDirection
      val offset = new Vector3f(aim.y / 3, -aim.x / 3, 0);

      //                    init bullet 1
      val bullet = getSpatial("Bullet")
      var finalOffset = aim.add(offset).mult(30)
      var trans = player.getLocalTranslation().add(finalOffset)
      bullet.setLocalTranslation(trans)
      bullet.addControl(new BulletControl(aim, settings.getWidth(), settings.getHeight()))
      bulletNode.attachChild(bullet);

      //                    init bullet 2
      val bullet2 = getSpatial("Bullet")
      finalOffset = aim.add(offset.negate()).mult(30)
      trans = player.getLocalTranslation().add(finalOffset)
      bullet2.setLocalTranslation(trans)
      bullet2.addControl(new BulletControl(aim, settings.getWidth(), settings.getHeight()))
      bulletNode.attachChild(bullet2)
    }
  }

  override def onAnalog(name: String, value: Float, tpf: Float): Unit = {
    System.out.println(name)
    if (player.getUserData[Boolean](Alive)) {
      name match {
        case MouseClick => launchTwoBullets
        case _ =>
      }
    }
  }

  def getSpawnPosition: Vector3f = {
    var pos: Vector3f = new Vector3f(new Random().nextInt(settings.getWidth()), new Random().nextInt(settings.getHeight()), 0)
    do {
      pos = new Vector3f(new Random().nextInt(settings.getWidth()), new Random().nextInt(settings.getHeight()), 0)
    } while (pos.distanceSquared(player.getLocalTranslation()) < 8000)
    pos
  }


  def createSeeker = {
    val seeker = getSpatial("Seeker")
    seeker.setLocalTranslation(getSpawnPosition)
    seeker.addControl(new SeekerControl(player))
    seeker.setUserData("active", false)
    enemyNode.attachChild(seeker)
  }


  def createWanderer = {
    val wanderer = getSpatial("Wanderer")
    wanderer.setLocalTranslation(getSpawnPosition)
    wanderer.addControl(new WandererControl())
    wanderer.setUserData("active", false)
    enemyNode.attachChild(wanderer)
  }

  def spawnEnemies = {
    if (System.currentTimeMillis() - enemySpawnCooldown >= 17) {
      enemySpawnCooldown = System.currentTimeMillis

      if (enemyNode.getQuantity < 50) {
        if (new Random().nextInt(enemySpawnChance.toInt) == 0) {
          createSeeker
        }
        if (new Random().nextInt(enemySpawnChance.toInt) == 0) {
          createWanderer
        }
      }
      //increase Spawn Time
      if (enemySpawnChance >= 1.1f) {
        enemySpawnChance -= 0.005f
      }
    }
  }

  override def simpleUpdate(tpf: Float): Unit = {
    if (player.getUserData[Boolean](Alive)) {
      spawnEnemies
    }
  }
}