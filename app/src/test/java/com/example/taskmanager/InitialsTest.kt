package com.example.taskmanager

import com.example.taskmanager.core.ui.component.initialsOf
import org.junit.Assert.assertEquals
import org.junit.Test

class InitialsTest {
    @Test fun twoLastWords() = assertEquals("VA", initialsOf("Nguyễn Văn An"))
    @Test fun singleWord() = assertEquals("A", initialsOf("an"))
    @Test fun extraSpaces() = assertEquals("NA", initialsOf("  Nguyễn   An "))
    @Test fun empty() = assertEquals("?", initialsOf(""))
}
