import com.google.gson.Gson
import dev.inmo.micro_utils.common.MPPFile
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.api.send.media.sendPhoto
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.extensions.utils.extensions.sameChat
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.urlButton
import dev.inmo.tgbotapi.requests.abstracts.InputFile
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.api.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.util.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

const val pathPhoto = "D:\\Burak\\Projects\\BelGIMBot\\src\\main\\resources\\"
const val ADMIN_PASSWORD = "v13"
val users_orders: MutableMap<String, ClientInputData> = mutableMapOf()
val stateList: MutableMap<String, State> = mutableMapOf()
val user_messages: MutableMap<String, TextMessage?> = mutableMapOf()

@OptIn(RiskFeature::class)
suspend fun main() {
    val token = "7110904292:AAH2hztvv6yArldmlZJISjf0p_a1wvSR1p0"
    val bot = telegramBot(token)

    bot.buildBehaviourWithLongPolling {
        println(getMe())
        setMyCommands(BotCommand("start", "Запуск бота"))
        onCommand("start") {
            println(getMe())
            println("Started User: ${it.from?.username} ${it.from?.id} ${it.from?.firstName} ${it.from?.lastName} ${it.chat.id}")
            users_orders[it.chat.id.toString()] = ClientInputData(clientCode = null, orderCode = null, orderDate = null)
            bot.sendTextMessage(
                it.chat.id,
                "Информационный бот, для отображения информации, позволяющий помочь пользователям БелГИМ",
                replyMarkup = Menu.startMenu
            )
        }

        onDataCallbackQuery("answer") {
            stateList[it.from.id.toString()] = State.InputAnswer
            val idClient = waitTextMessage(
                SendTextMessage(it.from.id, "Введите id клиента, которому хотите ответить:")
            ).filter { item ->
                item.sameChat(it.from.id) && stateList[it.from.id.toString()] == State.InputAnswer
            }.first().text

            if( stateList[it.from.id.toString()] != State.InputAnswer) {
                return@onDataCallbackQuery
            }

            val message: TextMessage = waitTextMessage(
                SendTextMessage(it.from.id, "Введите текст сообщения для клиента:")
            ).filter { item ->
                item.sameChat(it.from.id)
            }.first()

            if( stateList[it.from.id.toString()] != State.InputAnswer) {
                return@onDataCallbackQuery
            }

            if (user_messages["ChatId(chatId=$idClient)"] == null) {
                bot.sendTextMessage(it.from.id, "Ваш вопрос не найден, попробуйте задать его ещё раз")
                bot.sendTextMessage(it.from.id, "Главное меню", replyMarkup = Menu.mainMenu)
            } else {
                val idToSend = ChatId(chatId = RawChatId(idClient?.toLong() ?: 0))
                bot.sendTextMessage(idToSend, "Ответ на Ваш вопрос")
                user_messages["ChatId(chatId=$idClient)"]?.let { question -> bot.reply(question, message) }
                user_messages["ChatId(chatId=$idClient)"] = null
                bot.sendTextMessage(idToSend, "Всегда рады Вам помочь!")
                bot.sendTextMessage(it.from.id, "Добро пожаловать в систему управления", replyMarkup = Menu.adminMenu)
            }
        }

        onCommand("admin_panel") {
            stateList[it.from?.id.toString()] = State.InputPassword
            val message = waitTextMessage(
                SendTextMessage(it.chat.id, "Введите пароль администратора:")
            ).filter { item ->
                item.sameChat(it.chat.id)
            }.first().text

            if (stateList[it.from?.id.toString()] != State.InputPassword) {
                return@onCommand
            }

            if (message == ADMIN_PASSWORD) {
                bot.sendMessage(it.chat.id, "Добро пожаловать в систему управления", replyMarkup = Menu.adminMenu)
            }
        }

        onDataCallbackQuery("user_messages") {
            var counterMessages = 0
            for ((k, v) in user_messages) {
                if (v != null) {
                    bot.sendTextMessage(it.from.id, "$k: ${v.text}")
                    counterMessages++
                }
            }
            if (counterMessages == 0) {
                bot.sendTextMessage(it.from.id, "Сообщений нет", replyMarkup = Menu.backToMainMenu)
            } else bot.sendTextMessage(it.from.id, "Выберите действие", replyMarkup = Menu.adminMenu)
        }

        onDataCallbackQuery("menu") {
            bot.sendTextMessage(
                chatId = it.from.id,
                text = "Вот список всех доступных опций",
                replyMarkup = Menu.mainMenu
            )
        }

        onDataCallbackQuery("question") {
            stateList[it.from.id.toString()] = State.InputQuestion
            user_messages[it.from.id.toString()] = waitTextMessage(
                SendTextMessage(it.from.id, "Введите текст Вашего сообщения")
            ).filter { item ->
                item.sameChat(it.from.id) && stateList[it.from.id.toString()] == State.InputQuestion
            }.first()

            if (stateList[it.from.id.toString()] != State.InputQuestion) {
                user_messages[it.from.id.toString()] = null
                return@onDataCallbackQuery
            }

            println("Сообщение от пользователя ${it.from.firstName} ${it.from.lastName}  чат-ID ${it.from.id}): ${user_messages[it.from.id.toString()]?.text} \n")

            bot.sendTextMessage(it.from.id, "Ваше сообщение отправлено!")
            bot.sendTextMessage(it.from.id, "Главное меню", replyMarkup = Menu.mainMenu)
        }

        onDataCallbackQuery("check") {
            stateList[it.from.id.toString()] = State.InputOrderData
            users_orders[it.from.id.toString()]!!.clientCode = waitTextMessage(
                SendTextMessage(it.from.id, "Введите код клиента:")
            ).filter { item ->
                item.sameChat(it.from.id)
            }.first()

            println("Пользователь ${it.from.firstName} ${it.from.lastName} ввел код клиента [проверка готовности приборов]: ${users_orders[it.from.id.toString()]!!.clientCode?.text}")

            if (stateList[it.from.id.toString()] != State.InputOrderData)
                return@onDataCallbackQuery
            else {
                users_orders[it.from.id.toString()]!!.orderCode = waitTextMessage(
                    SendTextMessage(it.from.id, "Введите код квитанции:")
                ).filter { item ->
                    item.sameChat(it.from.id)
                }.first()

                println("Пользователь ${it.from.firstName} ${it.from.lastName} ввел номер квитанции [проверка готовности приборов]: ${users_orders[it.from.id.toString()]!!.orderCode?.text}")
            }

            if (stateList[it.from.id.toString()] != State.InputOrderData)
                return@onDataCallbackQuery
            else {
                users_orders[it.from.id.toString()]!!.orderDate = waitTextMessage(
                    SendTextMessage(
                        it.from.id,
                        "Введите дату квитанции\n(формат даты:ГГГГ-ММ-ДД)\nНапример(2024-01-31):"
                    )
                ).filter { item ->
                    item.sameChat(it.from.id)
                }.first()
                if (stateList[it.from.id.toString()] != State.InputOrderData) {
                    return@onDataCallbackQuery
                }
                println("Пользователь ${it.from.firstName} ${it.from.lastName} ввел следующее сообщение дату [проверка готовности приборов]: ${users_orders[it.from.id.toString()]!!.orderDate?.text} ")

                try {
                    val objectList = checkOrdersInfo(users_orders[it.from.id.toString()]!!)
                    if (objectList.isEmpty()) {
                        bot.sendTextMessage(
                            it.from.id,
                            "Данные по Вашему запросу не найдены! Проверьте корректность введенных данных",
                        )
                        bot.sendTextMessage(
                            it.from.id,
                            showInfoOrder(users_orders[it.from.id.toString()]!!),
                            replyMarkup = Menu.repeatMenu
                        )
                    } else {
                        var result = ""
                        objectList.forEach { responseOrderInfo ->
                            result += "${responseOrderInfo.code} Готов: ${if (responseOrderInfo.ready) "Да" else "Нет"} \n"
                        }

                        bot.sendTextMessage(it.from.id, result)
                        val readyMenu = inlineKeyboard {
                            row {
                                urlButton(
                                    "Да",
                                    "https://belgim.by/readiness/schedule-out?clientCode=${users_orders[it.from.id.toString()]!!.clientCode?.text}&orderCode=${users_orders[it.from.id.toString()]!!.orderCode?.text}"
                                )
                            }
                            row {
                                dataButton("Нет (возврат на главное меню)", "menu")
                            }
                        }
                        bot.sendTextMessage(
                            it.from.id, "Желаете воспользоваться предварительной записью?",
                            replyMarkup = readyMenu
                        )
                    }
                } catch (e: Exception) {
                    users_orders[it.from.id.toString()] = users_orders[it.from.id.toString()]!!.copy(
                        clientCode = null,
                        orderCode = null,
                        orderDate = null
                    )
                    bot.sendTextMessage(
                        it.from.id, "Что-то пошле не так! Проблемы с сервером",
                        replyMarkup = Menu.mainMenu
                    )
                }
            }
        }

        onDataCallbackQuery("site") {
            bot.sendTextMessage(it.from.id, "Выберите интересующий Вас ресурс:", replyMarkup = Menu.siteMenu)
        }

        onDataCallbackQuery("MainRoute") {
            val photoPath = "${pathPhoto}main_route.jpg"
            bot.sendPhoto(it.from.id, InputFile.fromFile(MPPFile(photoPath)))
            bot.sendTextMessage(
                it.from.id,
                "Координаты для навигатора: 53.933973, 27.539918",
                replyMarkup = Menu.backToRouteMenu
            )
        }

        onDataCallbackQuery("BPRoute") {
            val photoPath = "${pathPhoto}bp_route.jpg"
            bot.sendPhoto(it.from.id, InputFile.fromFile(MPPFile(photoPath)))
            bot.sendTextMessage(
                it.from.id,
                "Координаты для навигатора: 53.933973, 27.539918",
                replyMarkup = Menu.backToRouteMenu
            )
        }

        onDataCallbackQuery("SertRoute") {
            val photoPath = "${pathPhoto}sert_route.jpg"
            bot.sendPhoto(it.from.id, InputFile.fromFile(MPPFile(photoPath)))
            bot.sendTextMessage(
                it.from.id,
                "Координаты для навигатора: 53.937923, 27.542787",
                replyMarkup = Menu.backToRouteMenu
            )
        }

        onDataCallbackQuery("route") {
            bot.sendTextMessage(
                it.from.id,
                "К какому объекту Вас интересует схема проезда?",
                replyMarkup = Menu.routeMenu
            )
        }

        onDataCallbackQuery("edit_code_client") {
            stateList[it.from.id.toString()] = State.InputOrderDataRepeat
            val oldClientNumber = users_orders[it.from.id.toString()]!!.clientCode?.text
            users_orders[it.from.id.toString()]!!.clientCode = waitTextMessage(
                SendTextMessage(it.from.id, "Введите новый код клиента:")
            ).filter { item ->
                item.sameChat(it.from.id)
            }.first()

            if (  stateList[it.from.id.toString()] != State.InputOrderDataRepeat) return@onDataCallbackQuery
            println("Пользователь ${it.from.firstName} ${it.from.lastName} изменил код клиента: c $oldClientNumber на ${users_orders[it.from.id.toString()]!!.clientCode?.text}")

            bot.sendTextMessage(
                it.from.id,
                showInfoOrder(users_orders[it.from.id.toString()]!!),
                replyMarkup = Menu.repeatMenu
            )
        }

        onDataCallbackQuery("edit_code_orders") {
            stateList[it.from.id.toString()] = State.InputOrderDataRepeat
            val oldClientOrder = users_orders[it.from.id.toString()]!!.orderCode?.text
            users_orders[it.from.id.toString()]!!.orderCode = waitTextMessage(
                SendTextMessage(it.from.id, "Введите новый код квитанции:")
            ).filter { item ->
                item.sameChat(it.from.id)
            }.first()

            if ( stateList[it.from.id.toString()] != State.InputOrderDataRepeat) return@onDataCallbackQuery
            println("Пользователь ${it.from.firstName} ${it.from.lastName} изменил код заказа : c $oldClientOrder на ${users_orders[it.from.id.toString()]!!.orderCode?.text}")
            bot.sendTextMessage(
                it.from.id,
                showInfoOrder(users_orders[it.from.id.toString()]!!),
                replyMarkup = Menu.repeatMenu
            )
        }

        onDataCallbackQuery("edit_date_orders") {
            stateList[it.from.id.toString()] = State.InputOrderDataRepeat
            val oldDateOrder = users_orders[it.from.id.toString()]!!.orderDate?.text
            users_orders[it.from.id.toString()]!!.orderDate = waitTextMessage(
                SendTextMessage(it.from.id, "Введите новую дату квитанции:")
            ).filter { item ->
                item.sameChat(it.from.id)
            }.first()
            if (  stateList[it.from.id.toString()] != State.InputOrderDataRepeat) return@onDataCallbackQuery
            println("Пользователь ${it.from.firstName} ${it.from.lastName} изменил дату заказа : c $oldDateOrder на ${users_orders[it.from.id.toString()]!!.orderDate?.text}")
            bot.sendTextMessage(
                it.from.id,
                showInfoOrder(users_orders[it.from.id.toString()]!!),
                replyMarkup = Menu.repeatMenu
            )
        }

        onDataCallbackQuery("recheck") {
            val objectList = checkOrdersInfo(users_orders[it.from.id.toString()]!!)

            if (objectList.isEmpty()) {
                bot.sendTextMessage(
                    it.from.id,
                    "Данные по Вашему запросу не найдены! Проверьте корректность введенных данных",
                )
                bot.sendTextMessage(
                    it.from.id,
                    showInfoOrder(users_orders[it.from.id.toString()]!!),
                    replyMarkup = Menu.repeatMenu
                )

            } else {
                var result = ""
                objectList.forEach { responseOrderInfo ->
                    result += "${responseOrderInfo.code} Готов: ${if (responseOrderInfo.ready) "Да" else "Нет"} \n"
                }

                bot.sendTextMessage(it.from.id, result)
                val readyMenu = inlineKeyboard {
                    row {
                        urlButton(
                            "Да",
                            "https://belgim.by/readiness/schedule-out?clientCode=${users_orders[it.from.id.toString()]!!.clientCode?.text}&orderCode=${users_orders[it.from.id.toString()]!!.orderCode?.text}"
                        )
                    }
                    row {
                        dataButton("Нет (возврат на главное меню)", "menu")
                    }
                }
                bot.sendTextMessage(
                    it.from.id, "Желаете воспользоваться предварительной записью?",
                    replyMarkup = readyMenu
                )
            }
        }
    }.join()
}

@OptIn(RiskFeature::class)
suspend fun checkOrdersInfo(clientInputData: ClientInputData): List<ResponseOrderInfo> {
    val client = HttpClient(CIO)
    val response: HttpResponse =
        client.get(urlString = "https://api.belgim.by/bgim/get-works-date?clientCode=${clientInputData.clientCode?.text}&quittanceCode=${clientInputData.orderCode?.text}&orderDate=${clientInputData.orderDate?.text}")
    val body = response.bodyAsText()
    val gson = Gson()
    val objectList = gson.fromJson(body, Array<ResponseOrderInfo>::class.java).asList()
    client.close()
    return objectList
}

@OptIn(RiskFeature::class)
fun showInfoOrder(clientInputData: ClientInputData) =
    "Ваши введенные данные:\nКод клиента:${clientInputData.clientCode?.text}\nКод квитанции:${clientInputData.orderCode?.text}\nДата квитанции:${clientInputData.orderDate?.text}"