package com.example.taskmanager.core.ui

object Tags {
    const val EMAIL = "email"
    const val PASSWORD = "password"
    const val LOGIN_BUTTON = "login_button"

    const val FULL_NAME = "full_name"
    const val REGISTER_EMAIL = "register_email"
    const val REGISTER_PASSWORD = "register_password"
    const val CONFIRM_PASSWORD = "confirm_password"
    const val REGISTER_BUTTON = "register_button"
    const val TEAM_LIST = "team_list"
    const val TASK_LIST = "task_list"
    const val FAB_ADD_TASK = "fab_add_task"
    const val TASK_TITLE = "task_title"

    // Comment (CommentSection trong TaskDetail)
    const val COMMENT_LIST = "comment_list"
    const val COMMENT_INPUT = "comment_input"
    const val COMMENT_SEND = "comment_send"
    fun commentItem(id: String) = "comment_item_$id"

    // Team (NewTeamScreen)
    const val TEAM_NAME = "team_name"
    const val CREATE_TEAM_BUTTON = "create_team_button"

    const val BACK_BUTTON = "back_button"
}