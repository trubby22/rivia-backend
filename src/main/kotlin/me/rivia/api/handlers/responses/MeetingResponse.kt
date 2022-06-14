package me.rivia.api.handlers.responses

class MeetingResponse(val title:String, val startTime:Int, val endTime:Int,
val qualities:List<Double>, val responses:Int, val organizerId:String, val participants:List<Participant>,
                      val presetQs: Map<PresetQ, Int>, val feedbacks:List<String>
) {
}