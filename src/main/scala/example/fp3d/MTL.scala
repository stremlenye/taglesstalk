package example.fp3d

import java.util.UUID

import cats.{Monad, MonadError}
import cats.Functor
import cats.arrow.FunctionK
import cats.data.{EitherT, ReaderT}
import cats.implicits._
import cats.instances._

import scala.concurrent.Future
import scalaz.concurrent.Task

case class User(id: UUID, name : String)

trait UserAlgebra[F[_]] { self =>
  def getUser(id : UUID) : F[User]

  def mapK[G[_]](f: FunctionK[F, G]): UserAlgebra[G] =
    new UserAlgebra[G] {
      def getUser(id : UUID) : G[User] = f(self.getUser(id))
    }
}

trait UserStoreAlgebra[F[_]] { self =>
  def fetchUser(id : UUID): F[User]

  def mapK[G[_]](f: FunctionK[F, G]): UserStoreAlgebra[G] =
    new UserStoreAlgebra[G] {
      def fetchUser(id : UUID) : G[User] = f(self.fetchUser(id))
    }
}

trait PermissionAlgebra[F[_]] { self =>
  def isAllowed(id : UUID): F[Boolean]

  def mapK[G[_]](f: FunctionK[F, G]): PermissionAlgebra[G] =
    new PermissionAlgebra[G] {
      def isAllowed(id : UUID) : G[Boolean] = f(self.isAllowed(id))
    }
}

class UserStoreConfig

final class UserStore extends UserStoreAlgebra[ReaderT[Task, UserStoreConfig, ?]] {
  def fetchUser(id : UUID): ReaderT[Task, UserStoreConfig, User] = ???
}

class SecurityServiceException extends RuntimeException

final class SecurityService extends PermissionAlgebra[EitherT[Future, SecurityServiceException, ?]] {
  def isAllowed(id : UUID): EitherT[Future, SecurityServiceException, Boolean] = ???
}

abstract class UserServiceException extends RuntimeException
class UserServicePermissionException extends UserServiceException
class DependencyException(t: Throwable) extends UserServiceException

class UserService[F[_]](pa : PermissionAlgebra[F], us : UserStoreAlgebra[F])(
                       implicit F : MonadError[F, UserServiceException]
) extends UserAlgebra[F] {
  def getUser(id : UUID) : F[User] =
    for {
      allowed <- pa.isAllowed(id)
      _ <- if(allowed == false) F.raiseError[Unit](new UserServicePermissionException) else F.pure(())
      user <- us.fetchUser(id)
    } yield user
}

object UserService {
  type Context[A] =          EitherT[Future, UserServiceException, A]
  type UserStoreContext[A] = ReaderT[Task,   UserStoreConfig,      A]
  type PermissionsContext[A] = EitherT[Future, SecurityServiceException, A]

  implicit val taskFunctor : Functor[Task] = ???
  implicit val futureMonad : Monad[Future] = ???

  val userContextToContext: FunctionK[UserStoreContext, Context] = ???
  val permissionsContextToContext: FunctionK[PermissionsContext, Context] = ???

  val us : UserStoreAlgebra[Context] = new UserStore().mapK(userContextToContext)
  val pa : PermissionAlgebra[Context] = new SecurityService().mapK(permissionsContextToContext)

  val service = new UserService[Context](pa, us)
}
