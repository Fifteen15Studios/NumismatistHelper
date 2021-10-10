import javax.swing.JFrame
import javax.swing.JPanel

open class MyScreen() {

    var parent : JFrame = JFrame()
    var panel: JPanel = JPanel()

    constructor(parent : JFrame) : this() {
        this.parent = parent
    }
}