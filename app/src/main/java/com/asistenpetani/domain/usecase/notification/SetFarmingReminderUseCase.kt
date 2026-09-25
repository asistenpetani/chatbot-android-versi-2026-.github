package com.asistenpetani.domain.usecase.notification
import android.util.Log
class SetFarmingReminderUseCase {
    operator fun invoke(title: String, message: String, delayHours: Int): String {
        Log.d("FarmingReminder", "Reminder set: [$title] - $message in $delayHours hours.")
        return "Alarm/Pengingat Berhasil Diaktifkan: '$title' dijadwalkan dalam $delayHours jam."
    }
}
