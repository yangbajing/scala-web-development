package multi

object Utils {
  def isBlank(str: String): Boolean = str == null || str.isEmpty || str.forall(_ == ' ')
}