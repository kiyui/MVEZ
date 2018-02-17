package com.kiyui.timur.mvez

/**
 * All intents are in the form of an `Action`
 * where they are named and may carry a `value`
 * for the subscription to perform side-effects
 */
class Action(val name: String, val value: Any)