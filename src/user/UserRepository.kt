package no.echokarriere.user

import java.time.Instant
import java.util.UUID
import no.echokarriere.configuration.Argon2Configuration
import no.echokarriere.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class UserRepository(private val argon: Argon2Configuration) {
    suspend fun selectAll(): List<UserEntity> = dbQuery {
        Users.selectAll().map { toUser(it) }
    }

    suspend fun select(id: UUID): UserEntity? = dbQuery {
        Users
            .select { Users.id eq id }
            .mapNotNull { toUser(it) }
            .singleOrNull()
    }

    suspend fun create(user: UserDTO): UserEntity? {
        val generatedId = UUID.randomUUID()
        val created = dbQuery {
            Users
                .insertIgnore {
                    it[id] = generatedId
                    it[name] = user.name
                    it[email] = user.email
                    it[password] = argon.hash(user.password.toCharArray())
                    it[type] = user.type
                    it[createdAt] = Instant.now()
                }
                .resultedValues
        }

        if (created.isNullOrEmpty()) {
            throw RuntimeException("User not created")
        }

        return this.select(generatedId)
    }
}

private fun toUser(row: ResultRow): UserEntity = UserEntity(
    id = row[Users.id],
    name = row[Users.name],
    email = row[Users.email],
    password = row[Users.password],
    type = row[Users.type],
    createdAt = row[Users.createdAt],
    modifiedAt = row[Users.modifiedAt]
)