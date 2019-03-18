package example.fp3d

import java.util.UUID

import cats.data.EitherT
import cats.mtl.ApplicativeHandle
import scalaz.concurrent.Task

case class User(id: UUID, name : String)

trait UserAlgebra[F[_]] {
  def getUser(id : UUID) : F[User]
}

trait UserStoreAlgebra[F[_]] {
  def fetchUser(id : UUID): F[User]
}

trait PermissionAlgebra[F[_]] {
  def isAllowed(id : UUID): F[Boolean]
}

class DatabaseException extends RuntimeException

final class UserStore extends UserStoreAlgebra[EitherT[Task, DatabaseException, ?]] {
  def fetchUser(id : UUID): EitherT[Task, DatabaseException,User] = ???
}

class PermissionViolationException extends RuntimeException



class UserService[F[_]]()(implicit F : ApplicativeHandle[F, Throwable])
