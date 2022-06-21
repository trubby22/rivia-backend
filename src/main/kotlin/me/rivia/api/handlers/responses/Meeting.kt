package me.rivia.api.handlers.responses

class Meeting(
    var title: String,
    var startTime: Int,
    var endTime: Int,
    var qualities: List<Double>,
    var responses: Int,
    var organizerId: String,
    var users: List<UserData>,
    var presetQs: List<PresetQData>,
    var feedbacks: List<String>
)
