sealed interface State {
    data object InputQuestion: State
    data object InputOrderData: State
    data object InputOrderDataRepeat: State
    data object InputAnswer: State
    data object InputPassword: State
}