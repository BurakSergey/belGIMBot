import com.google.gson.Gson
import dev.inmo.micro_utils.common.MPPFile
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.send.media.sendPhoto
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitText
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.urlButton
import dev.inmo.tgbotapi.requests.abstracts.InputFile
import dev.inmo.tgbotapi.requests.send.SendTextMessage
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
import kotlinx.coroutines.flow.first

const val pathPhoto =  "D:\\Burak\\Projects\\BelGIMBot\\src\\main\\resources\\"

@OptIn(RiskFeature::class)
suspend fun main() {
    val token = "7110904292:AAH2hztvv6yArldmlZJISjf0p_a1wvSR1p0"
    val bot = telegramBot(token)

    bot.buildBehaviourWithLongPolling {
        println(getMe())
        var orderInfo = OrderInfo()

        onCommand("start") {
            println("Started User: ${it.from?.username} ${it.from?.id} ${it.from?.firstName} ${it.from?.lastName}")
            bot.sendTextMessage(
                it.chat.id,
                "Информационный бот, для отображения информации, позволяющий помочь пользователям БелГИМ",
                replyMarkup = Menu.startMenu
            )
        }

        onDataCallbackQuery("menu") {
            orderInfo = orderInfo.copy(clientCode = "", orderCode = "", orderData = "")
            bot.sendTextMessage(
                chatId = it.from.id,
                text = "Вот список всех доступных опций",
                replyMarkup = Menu.mainMenu
            )
        }

        onDataCallbackQuery("check") {
                orderInfo.clientCode = waitText(
                    SendTextMessage(it.from.id, "Введите код клиента:")
                ).first().text


            orderInfo.orderCode = waitText(
                SendTextMessage(it.from.id, "Введите код квитанции:")
            ).first().text

            orderInfo.orderData = waitText(
                SendTextMessage(it.from.id, "Введите дату квитанции\n(формат даты:ГГГГ-ММ-ДД)\nНапример(2024-01-31):")
            ).first().text

            val objectList = checkOrdersInfo(orderInfo)

            if (objectList.isEmpty()) {
                bot.sendTextMessage(
                    it.from.id,
                    "Данные по Вашему запросу не найдены! Проверьте корректность введенных данных",
                )
                bot.sendTextMessage(it.from.id, showInfoOrder(orderInfo), replyMarkup = Menu.repeatMenu)

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
                            "https://belgim.by/readiness/schedule-out?clientCode=${orderInfo.clientCode}&orderCode=${orderInfo.orderCode}"
                        )
                    }
                    row {
                        dataButton("Нет", "menu")
                    }
                }
                bot.sendTextMessage(
                    it.from.id, "Желаете воспользоваться предварительной записью?",
                    replyMarkup = readyMenu
                )
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
            orderInfo.clientCode = waitText(
                SendTextMessage(it.from.id, "Введите новый код клиента:")
            ).first().text

            bot.sendTextMessage(it.from.id, showInfoOrder(orderInfo), replyMarkup = Menu.repeatMenu)
        }

        onDataCallbackQuery("edit_code_orders") {
            orderInfo.orderCode = waitText(
                SendTextMessage(it.from.id, "Введите новый код квитанции:")
            ).first().text

            bot.sendTextMessage(it.from.id, showInfoOrder(orderInfo), replyMarkup = Menu.repeatMenu)
        }

        onDataCallbackQuery("edit_date_orders") {
            orderInfo.orderData = waitText(
                SendTextMessage(it.from.id, "Введите новую дату квитанции:")
            ).first().text

            bot.sendTextMessage(it.from.id, showInfoOrder(orderInfo), replyMarkup = Menu.repeatMenu)
        }

        onDataCallbackQuery("recheck") {
            val objectList = checkOrdersInfo(orderInfo)

            if (objectList.isEmpty()) {
                bot.sendTextMessage(
                    it.from.id,
                    "Данные по Вашему запросу не найдены! Проверьте корректность введенных данных",
                )
                bot.sendTextMessage(it.from.id, showInfoOrder(orderInfo), replyMarkup = Menu.repeatMenu)

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
                            "https://belgim.by/readiness/schedule-out?clientCode=${orderInfo.clientCode}&orderCode=${orderInfo.orderCode}"
                        )
                    }
                    row {
                        dataButton("Нет", "menu")
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

suspend fun checkOrdersInfo(orderInfo: OrderInfo): List<ResponseOrderInfo> {
    val client = HttpClient(CIO)
    val response: HttpResponse =
        client.get(urlString = "https://api.belgim.by/bgim/get-works-date?clientCode=${orderInfo.clientCode}&quittanceCode=${orderInfo.orderCode}&orderDate=${orderInfo.orderData}")
    val body = response.bodyAsText()
    val gson = Gson()
    val objectList = gson.fromJson(body, Array<ResponseOrderInfo>::class.java).asList()
    client.close()
    return objectList
}

fun showInfoOrder(orderInfo: OrderInfo) =
    "Ваши введенные данные:\nКод клиента:${orderInfo.clientCode}\nКод квитанции:${orderInfo.orderCode}\nДата квитанции:${orderInfo.orderData}"