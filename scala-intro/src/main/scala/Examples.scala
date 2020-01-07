// A simple example that demonstrates some scala features from a java developer perspective

// For a complete tour of scala features
// See https://docs.scala-lang.org/tour/tour-of-scala.html

// This is the equivalent of a main class
// Note the object keyword that defines a singleton object.
object Example1 extends App {

  // This is a simple class definition with one primary constructor that accepts one argument
  class Person(var age: Int) {

    // This is a member function definition
    def displayAge(): Unit = {
      println("Human age is: "+ age)
    }
  }

  // We can instantiate it like this
  val p1 = new Person(12)

  // This class is mutable
  p1.age = 33
  p1.displayAge()

}


// Demonstrate "case classes"
object Example2 extends App {

  // Scala supports "case classes"
  // See https://docs.scala-lang.org/tour/case-classes.html

  // Case classes are suitable for modeling IMMUTABLE data.


  // This is a trivial case class
  case class Person(age: Int)

  val p2 = Person(11) // Note that we don't need "new" to instantiate it

  // They provide default getter properties
  println("Case class age is: " +  p2.age)

  // They also provide default "toString" implementation
  println("Default toString is: " + p2)

  // Case classes are immutable if we want to alter the we need to create a new instance via "copy" method
  val p3 = p2.copy(age = 33)

  // Note that both p2 and p3 instances exist
  println(s"p1 = $p2 p3 = $p3") // This is scala string interpolation

  // Case classes are compared by structure NOT by reference
  // that is case classes provide a default "equals" implementation

  val p4 = Person(11)

  println( "p2 and p4 are equal: " + (p2 == p4))
}

// traits
// See https://docs.scala-lang.org/tour/traits.html
object Example3 extends App {


  // This is line  an java "interface"
  trait CanSpeak {
    def speak(): Unit
  }

  // Case classes can extend interfaces like ordinary classes
  case class Person(name: String) extends CanSpeak {
    override def speak: Unit = println("Hello my name is " + name)
  }

  val p1 = Person("fotis")
  p1.speak()
}

// Subtyping and pattern matching
object Example4 extends App {

  // We can model hierarchies of objects (dtos) using "traits" , "case classes" and "subtyping"
  trait Notification

  case class Email(sender: String) extends Notification

  case class SMS(phone: String) extends Notification

  val n = Email("fpaschos@gmail.com")

  // We can use pattern matching ( a switch statement on steroids) to deconstruct messages
  // see https://docs.scala-lang.org/tour/pattern-matching.html

  n match {
    case Email(s) => println("Received notification Email with sender " + s ) // Note that we can extract parameter s
    case _ => println("Received notification SMS") // Here "case _" means else
  }
}

// Companion objects objects
// See https://docs.scala-lang.org/tour/singleton-objects.html
object Example5 extends App {

  // Usually data definitions come in pairs of a "class" and an singleton "companion object" sharing the same name.
  // The companion object contains the "static" members and methods of a class

  case class Person(name: String) {
    def displayName() = println("My name is " + name)
  }

  object Person {
    // Like factory method
    def make(name: String) =  Person(name) // Person here refers to "case class Person"
  }

  val p = Person.make("fotis")
  p.displayName()
}
