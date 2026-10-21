package com.example.taskmanager.core.ui

object Tags {
    const val EMAIL = "email"
    const val PASSWORD = "password"
    const val LOGIN_BUTTON = "login_button"
    const val TEAM_LIST = "team_list"
    const val TASK_LIST = "task_list"
    const val FAB_ADD_TASK = "fab_add_task"
    const val TASK_TITLE = "task_title"

    // Comment (CommentSection trong TaskDetail)
    const val COMMENT_LIST = "comment_list"
    const val COMMENT_INPUT = "comment_input"
    const val COMMENT_SEND = "comment_send"
    fun commentItem(id: String) = "comment_item_$id"
}