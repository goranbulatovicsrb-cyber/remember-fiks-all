package com.podsetnikkapp.utils

import java.util.Calendar

data class Holiday(val name: String, val month: Int, val day: Int, val type: String = "")

object HolidayHelper {

    val orthodoxFixed = listOf(
        // JANUAR
        Holiday("Badnji dan (Pravoslavni)", 1, 6, "Pravoslavni"),
        Holiday("Bozic - Rождество Христово", 1, 7, "Pravoslavni"),
        Holiday("Sabor Presvete Bogorodice", 1, 8, "Pravoslavni"),
        Holiday("Sv. Stefan Prvomucenik - Stevandan", 1, 9, "Pravoslavni"),
        Holiday("Nova godina po Jul. kal. - Vasiljdan", 1, 14, "Pravoslavni"),
        Holiday("Bogojavljenje - Krstovdan", 1, 19, "Pravoslavni"),
        Holiday("Sv. Jovan Krstitelj - Jovanjdan", 1, 20, "Pravoslavni"),
        Holiday("Sv. Atanasije Veliki - Atanjasijevdan", 1, 18, "Pravoslavni"),
        Holiday("Sv. Sava Srpski - Savindan", 1, 27, "Pravoslavni"),

        // FEBRUAR
        Holiday("Sv. Trifun - Trifundan (vinogradari)", 2, 14, "Pravoslavni"),
        Holiday("Sretenje Gospodnje (Candlemas)", 2, 15, "Pravoslavni"),
        Holiday("Sv. Simeon Mirotocivi - Simeondan", 2, 26, "Pravoslavni"),

        // MART
        Holiday("Sv. 40 Sevastijskih muccenika", 3, 22, "Pravoslavni"),

        // APRIL
        Holiday("Blagovesti - Blagovijesti", 4, 7, "Pravoslavni"),
        Holiday("Lazareva subota", 4, 27, "Pravoslavni"),
        Holiday("Cveti - Cvjetna nedjelja (Ulazak u Jerusalim)", 4, 28, "Pravoslavni"),
        Holiday("Veliki Cetvrtak", 5, 2, "Pravoslavni"),
        Holiday("Veliki Petak (Stradanje Hristovo)", 5, 3, "Pravoslavni"),
        Holiday("Velika Subota", 5, 4, "Pravoslavni"),

        // MAJ
        Holiday("Vaskrs - Svetlo Vaskrsenje (Uskrs)", 5, 5, "Pravoslavni"),
        Holiday("Vaskrsni ponedjeljak - Svetla nedjelja", 5, 6, "Pravoslavni"),
        Holiday("Djurdjevdan - Sv. Georgije Pobedonosac", 5, 6, "Pravoslavni"),
        Holiday("Sv. Vasilije Ostroški Cudotvorac", 5, 12, "Pravoslavni"),
        Holiday("Sv. Nikola Prolecni (Letnji)", 5, 22, "Pravoslavni"),
        Holiday("Sv. Car Konstantin i carica Jelena", 6, 3, "Pravoslavni"),

        // JUN
        Holiday("Spasovdan - Vaznesenje Gospodnje", 6, 13, "Pravoslavni"),
        Holiday("Petrovacki post pocinje", 6, 16, "Pravoslavni"),
        Holiday("Sveta Trojica - Duhovi (Trojicin dan)", 6, 23, "Pravoslavni"),
        Holiday("Vidovdan - Sv. knez Lazar i kosovski muccenici", 6, 28, "Pravoslavni"),

        // JUL
        Holiday("Petrovdan - Sv. apostoli Petar i Pavle", 7, 12, "Pravoslavni"),
        Holiday("Sv. prorok Ilija - Ilindan", 8, 2, "Pravoslavni"),

        // AVGUST
        Holiday("Sv. Pantelejmon Iscelitelj - Pantelijedan", 8, 9, "Pravoslavni"),
        Holiday("Preobrazenje Gospodnje (Spasovdan ljetnji)", 8, 19, "Pravoslavni"),
        Holiday("Velika Gospojina - Uspenje Presvete Bogorodice", 8, 28, "Pravoslavni"),

        // SEPTEMBAR
        Holiday("Mala Gospojina - Rodjenje Presvete Bogorodice", 9, 21, "Pravoslavni"),
        Holiday("Krstovdan - Vozdvizenje Casnog Krsta", 9, 27, "Pravoslavni"),

        // OKTOBAR
        Holiday("Sv. Petka Tarnovska - Petkovdan", 10, 27, "Pravoslavni"),
        Holiday("Sv. Luka Apostol i Jevandjelist - Lukindan", 10, 31, "Pravoslavni"),

        // NOVEMBAR
        Holiday("Sv. Arhangel Mihailo - Arandjelovdan (Miholjdan)", 11, 21, "Pravoslavni"),
        Holiday("Sv. Andreja Prvozvani apostol", 12, 13, "Pravoslavni"),
        Holiday("Uvod Presvete Bogorodice u Hram", 12, 4, "Pravoslavni"),
        Holiday("Bozicni post pocinje (Filipovdan)", 11, 28, "Pravoslavni"),
        Holiday("Mitrovdan - Sv. mucenik Dimitrije", 11, 8, "Pravoslavni"),

        // DECEMBAR
        Holiday("Sv. Nikola Zimski - Nikoljdan", 12, 19, "Pravoslavni"),
        Holiday("Sv. Ignjatije Bogonosac", 12, 20, "Pravoslavni"),
        Holiday("Naum Ohridski Cudotvorac", 12, 23, "Pravoslavni")
    )

    // Slave - najcesci porodicni sveci
    val slaveDates = listOf(
        Holiday("SLAVA: Sv. Nikola (Nikoljdan) - zimski", 12, 19, "Slava"),
        Holiday("SLAVA: Sv. Nikola (Nikoljdan) - prolecni", 5, 22, "Slava"),
        Holiday("SLAVA: Sv. Jovan (Jovanjdan)", 1, 20, "Slava"),
        Holiday("SLAVA: Sv. Sava (Savindan)", 1, 27, "Slava"),
        Holiday("SLAVA: Sv. Trifun (Trifundan)", 2, 14, "Slava"),
        Holiday("SLAVA: Sv. Sretenje (Sretenjdan)", 2, 15, "Slava"),
        Holiday("SLAVA: Sv. Djordje / Djurdjevdan", 5, 6, "Slava"),
        Holiday("SLAVA: Sv. Arhangel Mihailo (Arandjeljovdan)", 11, 21, "Slava"),
        Holiday("SLAVA: Sv. Dimitrije (Mitrovdan)", 11, 8, "Slava"),
        Holiday("SLAVA: Sv. Ilija (Ilindan)", 8, 2, "Slava"),
        Holiday("SLAVA: Sv. Petka (Petkovdan)", 10, 27, "Slava"),
        Holiday("SLAVA: Sv. Luka apostol (Lukindan)", 10, 31, "Slava"),
        Holiday("SLAVA: Sv. Andreja Prvozvani", 12, 13, "Slava"),
        Holiday("SLAVA: Sv. Vasilije Ostroški", 5, 12, "Slava"),
        Holiday("SLAVA: Sv. Stefan (Stevandan)", 1, 9, "Slava"),
        Holiday("SLAVA: Sv. Pantelejmon (Pantelijedan)", 8, 9, "Slava"),
        Holiday("SLAVA: Sv. Paraskeva (Petkovdan)", 10, 27, "Slava"),
        Holiday("SLAVA: Sv. Petar i Pavle (Petrovdan)", 7, 12, "Slava"),
        Holiday("SLAVA: Sv. Vojvoda (Vidovdan)", 6, 28, "Slava"),
        Holiday("SLAVA: Sv. Vartolomej apostol", 6, 24, "Slava"),
        Holiday("SLAVA: Sv. prorok Jeremija", 5, 14, "Slava"),
        Holiday("SLAVA: Sv. Naum Ohridski", 12, 23, "Slava"),
        Holiday("SLAVA: Sv. Spiridon", 12, 25, "Slava"),
        Holiday("SLAVA: Sv. Vasilije Veliki (Vasiljdan)", 1, 14, "Slava"),
        Holiday("SLAVA: Sveta Trojica (Trojicin dan)", 6, 23, "Slava"),
        Holiday("SLAVA: Krstovdan - Sv. Kriz", 9, 27, "Slava"),
        Holiday("SLAVA: Sv. apostol Toma", 10, 19, "Slava"),
        Holiday("SLAVA: Sv. Marko Apostol", 4, 25, "Slava"),
        Holiday("SLAVA: Preobrazenje (Spasovdan)", 8, 19, "Slava"),
        Holiday("SLAVA: Sv. Apostol Jakov", 5, 13, "Slava")
    )

    val serbianState = listOf(
        Holiday("Nova godina", 1, 1, "Srbija"),
        Holiday("Nova godina - drugi dan", 1, 2, "Srbija"),
        Holiday("Bozic (pravoslavni)", 1, 7, "Srbija"),
        Holiday("Dan drzavnosti - Sretenje", 2, 15, "Srbija"),
        Holiday("Dan drzavnosti - drugi dan", 2, 16, "Srbija"),
        Holiday("Praznik rada", 5, 1, "Srbija"),
        Holiday("Praznik rada - drugi dan", 5, 2, "Srbija"),
        Holiday("Dan pobede nad fasizmom", 5, 9, "Srbija"),
        Holiday("Dan primirja u I svetskom ratu", 11, 11, "Srbija")
    )

    val bosnianState = listOf(
        Holiday("Nova godina", 1, 1, "BiH"),
        Holiday("Bozic (pravoslavni)", 1, 7, "BiH"),
        Holiday("Dan nezavisnosti BiH", 3, 1, "BiH"),
        Holiday("Bozic (katolicki)", 12, 25, "BiH"),
        Holiday("Praznik rada", 5, 1, "BiH"),
        Holiday("Dan pobjede", 5, 9, "BiH"),
        Holiday("Dan drzavnosti BiH", 11, 25, "BiH"),
        Holiday("Ramazan-bajram (1. dan)", 4, 10, "BiH"),
        Holiday("Kurban-bajram (1. dan)", 6, 17, "BiH")
    )

    val croatianState = listOf(
        Holiday("Nova godina", 1, 1, "Hrvatska"),
        Holiday("Sveta tri kralja", 1, 6, "Hrvatska"),
        Holiday("Uskrs", 3, 31, "Hrvatska"),
        Holiday("Uskrsni ponedjeljak", 4, 1, "Hrvatska"),
        Holiday("Praznik rada", 5, 1, "Hrvatska"),
        Holiday("Dan drzavnosti", 5, 30, "Hrvatska"),
        Holiday("Dan antifasisticke borbe", 6, 22, "Hrvatska"),
        Holiday("Dan pobjede i dom. zahvalnosti", 8, 5, "Hrvatska"),
        Holiday("Velika Gospa", 8, 15, "Hrvatska"),
        Holiday("Dan neovisnosti", 10, 8, "Hrvatska"),
        Holiday("Svi sveti", 11, 1, "Hrvatska"),
        Holiday("Bozic", 12, 25, "Hrvatska"),
        Holiday("Sveti Stjepan", 12, 26, "Hrvatska")
    )

    fun getHolidaysForCountry(country: String): List<Holiday> {
        return when (country) {
            "BA" -> bosnianState
            "HR" -> croatianState
            "ORTHODOX" -> orthodoxFixed.distinctBy { it.month * 100 + it.day + it.name.hashCode() % 100 }
            "SLAVA" -> slaveDates
            else -> serbianState
        }
    }

    // Shows ALL holidays for current + next year (no past filtering)
    fun getAllHolidaysForYear(country: String = "RS"): List<Pair<Holiday, Long>> {
        val holidays = getHolidaysForCountry(country)
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val result = mutableListOf<Pair<Holiday, Long>>()
        // Show this year's and next year's holidays
        for (yearOffset in 0..1) {
            for (holiday in holidays) {
                cal.set(currentYear + yearOffset, holiday.month - 1, holiday.day, 9, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                result.add(Pair(holiday, cal.timeInMillis))
            }
        }
        return result.sortedBy { it.second }.distinctBy { it.first.name }
    }

    fun getUpcomingHolidays(country: String = "RS", daysAhead: Int = 365): List<Pair<Holiday, Long>> {
        val holidays = getHolidaysForCountry(country)
        val now = System.currentTimeMillis()
        val result = mutableListOf<Pair<Holiday, Long>>()
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)

        for (holiday in holidays) {
            for (yearOffset in 0..1) {
                cal.set(currentYear + yearOffset, holiday.month - 1, holiday.day, 9, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val diff = (cal.timeInMillis - now) / (1000 * 60 * 60 * 24)
                if (diff in 0..daysAhead.toLong()) {
                    result.add(Pair(holiday, cal.timeInMillis))
                    break
                }
            }
        }
        return result.sortedBy { it.second }
    }
}
