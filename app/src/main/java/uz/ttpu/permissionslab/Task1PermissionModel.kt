package uz.ttpu.permissionslab

/**
 * 1-topshiriq. Kotlin modeli: rad etish holatlari ("Denied twice" modeli)
 *
 * PermissionModel klassi ruxsatnomalarning Android OS dagi dialog va rationale holatlarini modellashtiradi:
 * - granted: Ruxsat berilganligi
 * - rationale: Rationale (tushuntirish) ko'rsatilishi kerakligi (1 marta rad etilgandan so'ng true bo'ladi)
 * - dialogShows: Tizim muloqot oynasi (dialog) ko'rinishi
 */
class PermissionModel {
    var granted: Boolean = false
        private set

    var rationale: Boolean = false
        private set

    var dialogShows: Boolean = true
        private set

    private var denyCount: Int = 0

    fun request(userAllows: Boolean) {
        if (!dialogShows) {
            println("dialog yo'q, darhol rad")
            return
        }

        if (userAllows) {
            granted = true
            rationale = false
            dialogShows = false
        } else {
            denyCount++
            granted = false
            if (denyCount == 1) {
                rationale = true
                dialogShows = true
            } else if (denyCount >= 2) {
                rationale = false
                dialogShows = false
            }
        }
    }

    override fun toString(): String {
        return "granted=$granted, rationale=$rationale, dialogShows=$dialogShows"
    }
}

fun main() {
    println("--- 1-topshiriq: PermissionModel Sinovi ---")
    val model = PermissionModel()
    println("Boshlang'ich holat: $model")

    println("\n1-so'rov (Foydalanuvchi rad etdi):")
    model.request(userAllows = false)
    println("Natija: $model")

    println("\n2-so'rov (Foydalanuvchi yana rad etdi):")
    model.request(userAllows = false)
    println("Natija: $model")

    println("\n3-so'rov (Qayta so'rov yuborishga urinish):")
    model.request(userAllows = false)
    println("Natija: $model")

    println("\n4-so'rov (Yana so'rov yuborishga urinish):")
    model.request(userAllows = true)
    println("Natija: $model")
}
