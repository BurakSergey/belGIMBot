import dev.inmo.tgbotapi.extensions.utils.types.buttons.*
import dev.inmo.tgbotapi.types.buttons.SimpleKeyboardButton
import dev.inmo.tgbotapi.utils.row
import io.ktor.server.util.*

class Menu {
    companion object {

        val routeMenu = inlineKeyboard {
            row {
                dataButton("Главный вход","MainRoute")
            }
            row {
                dataButton("Бюро приемки","BPRoute")
            }
            row {
                dataButton("Отдел сертификации","SertRoute")
            }
            row {
                dataButton("Вернуться в Главное меню", "menu")
            }
        }

        val adminMenu = inlineKeyboard {
            row {
                dataButton("Просмотреть все сообщения", "user_messages")
            }
            row {
                dataButton("Ответить пользователю на сообщения", "answer")
            }
            row {
                dataButton("Вернуться в главное меню", "menu")
            }
        }

        val siteMenu = inlineKeyboard {
            row {
                urlButton("Главная страница","https://belgim.by/")
            }

            row {
                urlButton("Реквизиты", "https://belgim.by/pages/view?id=124")
            }

            row {
                webAppButton("Контакты", "https://belgim.by/pages/view?id=123")
            }

            row {
                urlButton("Новости", "https://belgim.by/news")
            }

            row {
                urlButton("Услуги", "https://belgim.by/pages/view?id=121")
            }

            row {
                urlButton("Сайт oei.by", "https://oei.by/")
            }

            row {
                dataButton("Вернуться в главное меню", "menu")
            }
        }
        val startMenu = inlineKeyboard {
            row {
                dataButton("Начать использовать бота!", "menu")
            }
        }
        val mainMenu = inlineKeyboard {
            row {
                dataButton("Проверить готовность приборов", "check")
            }
            row {
              dataButton("Навигация по сайту БелГИМ", "site")
            }
//            row {
//                dataButton("Количество клиентов в БелГИМ","queue")
//            }

            row {
                dataButton("Схемы проезда к объектам БелГИМ", "route")
            }
            row {
                dataButton("Задать вопрос","question")
            }
        }
        val backToMainMenu = inlineKeyboard {
            row {
                dataButton("Вернуться на главное меню", "menu")
            }
        }
        val backToRouteMenu = inlineKeyboard {
            row {
                dataButton("Вернуться к схемам", "route")
            }
            row {
                dataButton("Вернуться на главное меню", "menu")
            }
        }
        val checkMenu = inlineKeyboard {
            row {
                dataButton("Повторить проверку готовности приборов", "check")
            }

            row {
                dataButton("Перейти на главное меню", "menu")
            }
        }
        val repeatMenu = inlineKeyboard {
            row {
                dataButton("Изменить код заказчика", "edit_code_client")
            }
            row {
                dataButton("Изменить код квитанции", "edit_code_orders")
            }
            row {
                dataButton("Изменить дату квитанции", "edit_date_orders")
            }

            row {
                dataButton("Повторить проверку готовности приборов", "recheck")
            }

            row {
                dataButton("Перейти на главное меню", "menu")
            }
        }
    }
}