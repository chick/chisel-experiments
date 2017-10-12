
trait X {
  def x: Int
}

class Y(val x: Int) extends X

class Z(val x: Int) extends X

class W(override val x: Int) extends Y(x) {
  def f(w: Y): W = {
    new W(x + w.x)
  }
}

val a = new W(10).f(new Y(3))

