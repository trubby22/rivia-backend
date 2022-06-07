package me.rivia.api

data class MeetingPainPoint(val point_id: String?, val point_text: String?) {
    constructor() : this(null, null)
}
data class Participant(val participant_id: String?, val name: String?, val surname: String?, val email: String?) {
    constructor() : this(null, null, null, null)
}
data class Meeting(val title: String?, val start: Int?, val end: Int?) {
    constructor() : this(null, null, null)
}
class Response(val participant: Participant?, val quality: Float?, val points: Array<String>?, val not_needed: Array<Participant>?, val not_prepared: Array<Participant>?) {
    constructor() : this(null, null, null, null, null)
}
