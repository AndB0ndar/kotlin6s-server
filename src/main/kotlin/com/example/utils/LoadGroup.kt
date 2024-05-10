package com.example.utils

import com.example.services.GroupService


fun loadGroup(groupName: String, groupService: GroupService): Int {
    /*
    val url = "https://mirea.xyz/api/v1.3/"
    val certain = "groups/certain"
    val groupsData = withContext(Dispatchers.IO) {
        URL("$url$certain").readText()
    }
    val groupsList = mapper.readValue<List<Group>>(groupsData)

    for (group in groupsList) {
        val tGroup = TinyGroup.getOrCreate(group.groupName, group.groupSuffix)

        val data = withContext(Dispatchers.IO) {
            URL("$url$certain?name=${group.groupName}&suffix=${group.groupSuffix}").readText()
        }
        val groupData = mapper.readValue<List<DayScheduleData>>(data)

        for (groupDataItem in groupData) {
            val rGroup = RichGroup.getOrCreate(
                tGroup,
                groupDataItem.remoteFile,
                groupDataItem.unitName,
                groupDataItem.unitCourse,
                groupDataItem.updatedDate
            )

            for (dayScheduleData in groupDataItem.schedule) {
                val daySchedule = DaySchedule.getOrCreate(rGroup, dayScheduleData.day)

                for ((index, lessons) in listOf(dayScheduleData.odd, dayScheduleData.even).withIndex()) {
                    for (lessonList in lessons) {
                        for (lessonData in lessonList) {
                            Lesson.create(
                                daySchedule,
                                index == 1,
                                lessonData.name,
                                lessonData.type,
                                lessonData.tutor,
                                lessonData.place,
                                lessonData.link
                            )
                        }
                    }
                }
            }
        }
    }
     */
    val name= "ИКБО-01-21"
    val groupExists = groupService.getGroupByName(name) != null
    if (!groupExists) {
        val groupSuffix = "МОСИТ"
        val unitName = "Институт информационных технологий"
        val unitCourse = "Бакалавриат/специалитет, 3 курс"

        return groupService.createGroup(name, groupSuffix, unitName, unitCourse)
    }
    return -1
}