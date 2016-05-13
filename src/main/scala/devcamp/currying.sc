def add(a: Int)(b: Int) = a + b

val onePlusFive = add(1)(5)
val addFour = add(4)_
val twoPlusFour = addFour(2)

onePlusFive == twoPlusFour

def addA(x: Int, y: Int, z: Int): Int =
  x + y + z

val a = addA(1, 2, 3)

def addB(x: Int): Int => (Int => Int) =
  y => z => x + y + z

val b1 = addB(1)
val b2 = b1(2)
val b3 = b2(3)


case class Email(subject: String,  text: String,
                 sender: String, recipient: String)
type EmailFilter = Email => Boolean

case class User(name: String)
trait EmailRepository {
  def getMails(user: User, unread: Boolean): Seq[Email]
}
trait FilterRepository {
  def getEmailFilter(user: User): EmailFilter
}
trait MailboxService {
  def getNewMails(emailRepo: EmailRepository)(filterRepo: FilterRepository)(user: User) =
    emailRepo.getMails(user, unread = true).filter(filterRepo.getEmailFilter(user))
  val newMails: User => Seq[Email]
}

object MockEmailRepository extends EmailRepository {
  def getMails(user: User, unread: Boolean): Seq[Email] =
    Email(
      subject   = "Luke!",
      text      = "I am your father (shshshshshsh)",
      sender    = "darthvador@deathstar.com",
      recipient = "luke@rebellion.com") :: Nil
}
object MockFilterRepository extends FilterRepository {
  def getEmailFilter(user: User): EmailFilter = _ => true
}
object MailboxServiceWithMockDeps extends MailboxService {
  val newMails: User => Seq[Email] =
    getNewMails(MockEmailRepository)(MockFilterRepository) _
}

MailboxServiceWithMockDeps.newMails(User("Luke"))