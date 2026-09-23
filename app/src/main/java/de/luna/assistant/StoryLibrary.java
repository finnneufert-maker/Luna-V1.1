package de.luna.assistant;

/** Original Luna fantasy story. */
final class StoryLibrary {
    private StoryLibrary() {}

    static final String[] TITLES = {
            "Kapitel 1 – Das silberne Kätzchen",
            "Kapitel 2 – Stimmen des Waldes",
            "Kapitel 3 – Der erste Wassertropfen",
            "Kapitel 4 – Wind unter den Pfoten",
            "Kapitel 5 – Tee und Heilmagie",
            "Kapitel 6 – Der Weg hinaus",
            "Kapitel 7 – Der Tag, an dem der Himmel bebte",
            "Kapitel 8 – Die Spur der Vermissten",
            "Kapitel 9 – Eine unerwartete Rettung",
            "Kapitel 10 – Unterricht in Weiß und Schwarz",
            "Kapitel 11 – Tee für Reisende",
            "Kapitel 12 – Das Tor der Nordlicht-Akademie"
    };

    private static final String[] TEXTS = {
            "Im Großen Wald lebte Luna Yamanaka, ein Katzenmenschenkind mit silbernem Haar und einem viel zu neugierigen Schweif. Mit einem Holzschwert, einem Holzmesser und einem dunkelroten Schal erkundete sie jeden Pfad rund um ihr Dorf. Lesen konnte sie noch nicht besonders gut, doch sie wollte unbedingt erfahren, was hinter den hohen Bäumen lag.",
            "Luna lernte, dem Wald zuzuhören. Raschelnde Blätter verrieten ihr Tiere, knarrende Zweige fremde Schritte. Ihre feinen Sinne waren ein Geschenk, manchmal aber auch anstrengend. Die Dorfältesten brachten ihr bei, Ruhe zu bewahren und nicht jedem Geräusch hinterherzuspringen. Das gelang ihr meistens – außer wenn irgendwo ein besonders spannender Käfer landete.",
            "Mit sechs Jahren entdeckte Luna ihre Magie. Über ihrer Hand schwebte ein winziger Wassertropfen, der erst zitterte und dann mitten auf ihrer Nase zerplatzte. Sie übte geduldig weiter, bis aus dem Tropfen eine kleine Kugel wurde. Niemand im Dorf ahnte, dass dies der Anfang einer ungewöhnlichen Begabung war.",
            "Nach der Wassermagie kam der Wind. Luna ließ Blätter kreisen, kühlte heißen Tee und fing einmal versehentlich die Wäsche des halben Dorfes in einer Böe. Gleichzeitig übte sie weiter mit Schwert und Messer. Ihre Bewegungen wurden schnell und leise, doch ihre Lehrerin erinnerte sie daran, dass Können auch Verantwortung bedeutete.",
            "Als ein Kind sich am Knie verletzte, legte Luna beide Hände darum und stellte sich warmes Licht vor. Die kleine Wunde schloss sich. Später verband sie ihre Heilübungen mit einer Tasse beruhigendem Kräutertee. Daraus wurde ihr Markenzeichen: erst helfen, dann Tee reichen – und dabei so ernst schauen, dass niemand über ihre schief sitzende Schleife lachte.",
            "Luna begann eine Ausbildung in Haushalt, Kochen, Etikette und dem Dienst als Maid. Sie lernte, ein Tablett zu tragen, ohne dass ihr Schweif eine Tasse vom Tisch fegte. Schließlich erhielt sie ihre eigene schwarz-weiße Uniform. Als sie das Dorf verließ, nahm sie Holzschwert, Messer, Schal und Teeset mit. Der Wald blieb ihre Heimat, aber die Welt wartete.",
            "An einem stillen Nachmittag bebte der Himmel. Weit entfernt veränderte eine gewaltige magische Katastrophe das Leben unzähliger Menschen. Luna wurde nicht fortgerissen, doch die Tiere des Waldes flohen und fremde Magiespuren lagen in der Luft. Sie half im Dorf, versorgte Verletzte und begriff, dass ihre Reise nicht länger nur ein Abenteuer sein würde.",
            "Monate später hörte Luna von verschwundenen Tiermenschenkindern. Ihre Ohren fanden leise Rufe, ihre Nase fremde Lagerfeuer. Statt allein loszustürmen, beobachtete sie Wege und Wachen und hinterließ unauffällige Zeichen für mögliche Helfer. Zum ersten Mal verband sie Geduld, Magie und ihre Ausbildung zu einem richtigen Plan.",
            "Bei der Rettung traf Luna auf einen jungen Magier namens Ronan und dessen Reisegefährten. Luna schützte die Kinder mit einer Windbarriere und heilte ihre kleinen Verletzungen, während die anderen den Fluchtweg sicherten. Danach reichte sie allen Tee. Das Treffen war kurz, doch Luna merkte sich, dass auch sehr begabte Menschen Unterstützung brauchen konnten.",
            "In einer fernen Stadt vertiefte Luna ihre Maid-Ausbildung. Sie lernte höfliche Begrüßungen, sorgfältiges Kochen und das unauffällige Beobachten eines Raumes. Ihre Uniform wurde für Ohren und Schweif angepasst. Wenn eine Schleife verrutschte, nannte Luna es feierlich einen Grafikfehler und band sie mit unbewegter Miene neu.",
            "Auf der langen Reise half Luna, wo sie konnte. Sie kühlte Fieber mit Wassermagie, beruhigte erschöpfte Reisende mit Tee und vertrieb Gefahren lieber mit klug gelenktem Wind als mit einem Kampf. Gerüchte über eine katzenohrige Heilerin verbreiteten sich. Luna selbst hielt sie für übertrieben – auch wenn sie jedes Lob heimlich in ein kleines Notizbuch schrieb.",
            "Mit fünfzehn erreichte Luna die Nordlicht-Akademie. Vor dem großen Tor hielt sie kurz inne. Hinter ihr lagen der Wald, eine Katastrophe, neue Freunde und viele Tassen Tee; vor ihr lagen Bibliotheken, Magie und Fragen ohne Antwort. Sie strich ihre Uniform glatt, hob den Koffer und trat ein. Im selben Moment begann irgendwo eine Schulglocke zu läuten – und Lunas nächstes Abenteuer begann."
    };

    static String chapter(int index) {
        if (index < 0 || index >= TEXTS.length) return "Dieses Kapitel ist noch nicht verfügbar.";
        return TITLES[index] + ".\n\n" + TEXTS[index];
    }
}
