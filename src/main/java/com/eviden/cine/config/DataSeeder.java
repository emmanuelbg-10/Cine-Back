package com.eviden.cine.config;

import com.eviden.cine.model.*;
import com.eviden.cine.repository.*;
import com.eviden.cine.service.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DataSeeder {
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private final CloudinaryService cloudinaryService;



    @Bean
    public CommandLineRunner dataLoader(
            RoleRepository roleRepository,
            UserRepository userRepository,
            GenreRepository genreRepository,
            ClassificationRepository classificationRepository,
            MovieRepository movieRepository,
            FavoriteRepository favoriteRepository,
            RoomRepository roomRepository,
            AsientoRepository asientoRepository,
            EmisionRepository emisionRepository,
            TicketRepository ticketRepository,
            DirectorRepository directorRepository,
            ActorRepository actorRepository,
            RegionRepository regionRepository,
            ReservationRepository reservationRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            insertarRoles(roleRepository);
            insertarUsuarios(userRepository, roleRepository, passwordEncoder);
            insertarGeneros(genreRepository);
            insertarClasificaciones(classificationRepository);
            insertarPeliculas(movieRepository, genreRepository, classificationRepository);
            insertarRegiones();
            insertarSalasYAsientos(roomRepository, asientoRepository);
            insertarFavoritos(favoriteRepository, userRepository, movieRepository);

            insertarTickets(ticketRepository);
            insertarReservas(reservationRepository, emisionRepository, userRepository, ticketRepository); // << AQUI

        };
    }

    // Roles
    private void insertarRoles(RoleRepository roleRepository) {
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                    Role.builder().name("USER").build(),
                    Role.builder().name("ADMIN").build()
            ));
            logger.info("Roles insertados");
        }
    }

    private void insertarUsuarios(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder) {
        if (userRepository.count() == 0) {

            Role roleUser = roleRepository.findByName("USER").orElseThrow();
            Role roleAdmin = roleRepository.findByName("ADMIN").orElseThrow();

            // Usuarios

            userRepository.saveAll(List.of(
                    new User(roleUser, "Juanito", "juanito@mail.com", encoder.encode("123456"), "es", "ES"),
                    new User(roleAdmin, "Marta", "marta@mail.com", encoder.encode("123456"), "es", "ES"),
                    new User(roleUser, "Carlos89", "carlos89@mail.com", encoder.encode("123456"), "en", "US"),
                    new User(roleUser, "Lucia_23", "lucia23@mail.com", encoder.encode("123456"), "es", "ES"),
                    new User(roleAdmin, "AdminPro", "adminpro@mail.com", encoder.encode("123456"), "en", "UK")
            ));
            logger.info("Usuarios insertados");
        }
    }

    // Géneros
    private void insertarGeneros(GenreRepository genreRepository) {
        if (genreRepository.count() == 0) {
            genreRepository.saveAll(List.of(
                    Genre.builder().name("Acción").nameEn("Action").nameFr("Action").nameDe("Aktion").nameIt("Azione").namePt("Ação").build(),
                    Genre.builder().name("Comedia").nameEn("Comedy").nameFr("Comédie").nameDe("Komödie").nameIt("Commedia").namePt("Comédia").build(),
                    Genre.builder().name("Drama").nameEn("Drama").nameFr("Drame").nameDe("Drama").nameIt("Dramma").namePt("Drama").build(),
                    Genre.builder().name("Ciencia Ficción").nameEn("Science Fiction").nameFr("Science-fiction").nameDe("Science-Fiction").nameIt("Fantascienza").namePt("Ficção científica").build(),
                    Genre.builder().name("Animación").nameEn("Animation").nameFr("Animation").nameDe("Animation").nameIt("Animazione").namePt("Animação").build(),
                    Genre.builder().name("Terror").nameEn("Horror").nameFr("Horreur").nameDe("Horror").nameIt("Horror").namePt("Terror").build(),
                    Genre.builder().name("Aventura").nameEn("Adventure").nameFr("Aventure").nameDe("Abenteuer").nameIt("Avventura").namePt("Aventura").build(),
                    Genre.builder().name("Romance").nameEn("Romance").nameFr("Romance").nameDe("Romanze").nameIt("Romantico").namePt("Romance").build(),
                    Genre.builder().name("Fantasía").nameEn("Fantasy").nameFr("Fantastique").nameDe("Fantasie").nameIt("Fantasy").namePt("Fantasia").build(),
                    Genre.builder().name("Documental").nameEn("Documentary").nameFr("Documentaire").nameDe("Dokumentarfilm").nameIt("Documentario").namePt("Documentário").build(),
                    Genre.builder().name("Musical").nameEn("Musical").nameFr("Comédie musicale").nameDe("Musicalfilm").nameIt("Musical").namePt("Musical").build(),
                    Genre.builder().name("Suspenso").nameEn("Thriller").nameFr("Thriller").nameDe("Thriller").nameIt("Thriller").namePt("Suspense").build()
            ));
            logger.info("Géneros insertados con traducción a 6 idiomas");
        }
    }



    // Clasificaciones
    private void insertarClasificaciones(ClassificationRepository classificationRepository) {

        if (classificationRepository.count() == 0) {
            classificationRepository.saveAll(List.of(
                    Classification.builder().name("PG18").build(),
                    Classification.builder().name("PG16").build(),
                    Classification.builder().name("PG13").build(),
                    Classification.builder().name("PG8").build(),
                    Classification.builder().name("PG6").build(),
                    Classification.builder().name("PG0").build()
            ));
            logger.info("Clasificaciones insertadas");
        }
    }
    private void insertarPeliculas(MovieRepository movieRepository, GenreRepository genreRepository, ClassificationRepository classificationRepository) {
        if (movieRepository.count() > 0) return;

        Genre accion    = genreRepository.findByName("Acción").orElseThrow();
        Genre fantasia  = genreRepository.findByName("Fantasía").orElseThrow();
        Genre suspenso  = genreRepository.findByName("Suspenso").orElseThrow();
        Genre comedia   = genreRepository.findByName("Comedia").orElseThrow();
        Genre drama     = genreRepository.findByName("Drama").orElseThrow();
        Genre romance   = genreRepository.findByName("Romance").orElseThrow();
        Genre terror    = genreRepository.findByName("Terror").orElseThrow();
        Genre animacion = genreRepository.findByName("Animación").orElseThrow();
        Genre sciFi     = genreRepository.findByName("Ciencia Ficción").orElseThrow();

        Classification pg13 = classificationRepository.findByName("PG13").orElseThrow();
        Classification pg6  = classificationRepository.findByName("PG6").orElseThrow();
        Classification pg18 = classificationRepository.findByName("PG18").orElseThrow();
        Classification pg8  = classificationRepository.findByName("PG8").orElseThrow();
        Classification pg0  = classificationRepository.findByName("PG0").orElseThrow();
        Classification pg16  = classificationRepository.findByName("PG16").orElseThrow();


        Map<String, Director> directorCache = new HashMap<>();
        Map<String, Actor>    actorCache    = new HashMap<>();

        List<Movie> movies = List.of(
                Movie.builder()
                        .title("Titanic")
                        .titleEn("Titanic")
                        .titleFr("Titanic")
                        .titleDe("Titanic")
                        .titleIt("Titanic")
                        .titlePt("Titanic")
                        .synopsis("Jack (DiCaprio), un joven artista, en una partida de cartas gana un pasaje para América, en el Titanic, el trasatlántico más grande y seguro jamás construido. A bordo, conoce a Rose (Kate Winslet), una joven de una buena familia venida a menos que va a contraer un matrimonio de conveniencia con Cal (Billy Zane), un millonario engreído a quien sólo interesa el prestigioso apellido de su prometida. Jack y Rose se enamoran, pero Cal y la madre de Rose ponen todo tipo de trabas a su relación. Inesperadamente, un inmenso iceberg pone en peligro la vida de los pasajeros.")
                        .synopsisEn("Jack (DiCaprio), a young artist, wins a ticket to America in a card game aboard the Titanic, the largest and safest ocean liner ever built. Onboard, he meets Rose (Kate Winslet), a young woman from a declining wealthy family who is set to marry Cal (Billy Zane), an arrogant millionaire only interested in her prestigious family name. Jack and Rose fall in love, but Cal and Rose’s mother do everything to separate them. Suddenly, a massive iceberg threatens the lives of everyone onboard.")
                        .synopsisFr("Jack (DiCaprio), un jeune artiste, gagne un billet pour l'Amérique lors d'une partie de cartes, à bord du Titanic, le plus grand et le plus sûr paquebot jamais construit. À bord, il rencontre Rose (Kate Winslet), une jeune femme d'une famille aisée en déclin, promise à un mariage de convenance avec Cal (Billy Zane), un millionnaire arrogant intéressé uniquement par le prestige de son nom. Jack et Rose tombent amoureux, mais Cal et la mère de Rose entravent leur relation. Un immense iceberg met alors tous les passagers en danger.")
                        .synopsisDe("Jack (DiCaprio), ein junger Künstler, gewinnt bei einem Kartenspiel ein Ticket nach Amerika auf der Titanic, dem größten und sichersten jemals gebauten Ozeandampfer. An Bord lernt er Rose (Kate Winslet) kennen, eine junge Frau aus einer verarmten Adelsfamilie, die mit Cal (Billy Zane) verheiratet werden soll, einem arroganten Millionär, dem nur ihr Familienname wichtig ist. Jack und Rose verlieben sich, doch Cal und Roses Mutter versuchen alles, um sie zu trennen. Plötzlich bringt ein riesiger Eisberg das Leben aller an Bord in Gefahr.")
                        .synopsisIt("Jack (DiCaprio), un giovane artista, vince un biglietto per l’America in una partita a carte a bordo del Titanic, il più grande e sicuro transatlantico mai costruito. A bordo conosce Rose (Kate Winslet), una giovane donna di una famiglia nobile decaduta, promessa sposa di Cal (Billy Zane), un milionario arrogante interessato solo al nome prestigioso della ragazza. Jack e Rose si innamorano, ma Cal e la madre di Rose ostacolano il loro amore. Improvvisamente, un gigantesco iceberg mette in pericolo la vita di tutti i passeggeri.")
                        .synopsisPt("Jack (DiCaprio), um jovem artista, ganha em um jogo de cartas uma passagem para a América a bordo do Titanic, o maior e mais seguro transatlântico já construído. A bordo, ele conhece Rose (Kate Winslet), uma jovem de uma família nobre em decadência, prometida a Cal (Billy Zane), um milionário arrogante interessado apenas no nome prestigiado de sua noiva. Jack e Rose se apaixonam, mas Cal e a mãe de Rose fazem de tudo para separá-los. De repente, um enorme iceberg ameaça a vida de todos os passageiros.")
                        .time(195)
                        .genre(romance)
                        .classification(pg18)
                        .director(resolveDirector("James Cameron", directorCache))
                        .casting(resolveCasting(List.of("Leonardo DiCaprio", "Kate Winslet"), actorCache))
                        .urlImageX("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823888/m2ttwr632tpfpoqbmh3p.jpg")
                        .urlImageY("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823890/d0czvaayapfqtsnkf6mk.jpg")
                        .urlTrailer("https://www.youtube.com/watch?v=I7c1etV7D7g")
                        .releaseDate(LocalDate.of(1997, 12, 19))
                        .isAvailable(true)
                        .isComingSoon(true)
                        .build(),

                Movie.builder()
                        .title("Avatar").titleEn("Avatar").titleFr("Avatar").titleDe("Avatar").titleIt("Avatar").titlePt("Avatar")
                        .synopsis("En el año 2154, Jake Sully, un exmarine parapléjico, es seleccionado para participar en el programa Avatar en Pandora, una luna habitada por los Na'vi, una especie humanoide. Mediante un avatar, Jake explora este mundo y se infiltra en la tribu local, pero cuanto más conoce su cultura, más se cuestiona las intenciones humanas. Mientras el conflicto entre humanos y Na'vi escala, Jake deberá decidir de qué lado está.")
                        .synopsisEn("In the year 2154, paraplegic former Marine Jake Sully is chosen to join the Avatar program on Pandora, a moon inhabited by the Na'vi, a humanoid species. Using his avatar, Jake explores this vibrant world and infiltrates the local tribe. As he learns more about their way of life, he begins to question the human mission. As tensions rise between the Na'vi and humans, Jake must choose where his loyalty lies.")
                        .synopsisFr("En 2154, Jake Sully, un ancien marine paraplégique, est choisi pour participer au programme Avatar sur Pandora, une lune habitée par les Na'vi. Grâce à son avatar, Jake découvre ce monde extraordinaire et s’intègre à la tribu locale. En découvrant leur culture, il remet en question les intentions humaines. Alors que le conflit s’intensifie, Jake doit choisir son camp.")
                        .synopsisDe("Im Jahr 2154 wird der querschnittsgelähmte Ex-Marine Jake Sully für das Avatar-Programm auf Pandora ausgewählt, einem Mond, der von den Na'vi bewohnt wird. Durch seinen Avatar erkundet Jake die Welt und wird Teil des Stammes. Je mehr er über deren Lebensweise lernt, desto mehr stellt er die menschliche Mission in Frage. Im Angesicht eines drohenden Konflikts muss Jake eine Entscheidung treffen.")
                        .synopsisIt("Nel 2154, Jake Sully, un ex marine paraplegico, viene selezionato per il programma Avatar su Pandora, una luna abitata dai Na'vi. Attraverso il suo avatar, Jake esplora il mondo e si immerge nella cultura dei Na'vi. Col tempo, comincia a dubitare della missione umana e si trova diviso tra due mondi in conflitto.")
                        .synopsisPt("No ano de 2154, Jake Sully, um ex-fuzileiro paraplégico, é escolhido para o programa Avatar em Pandora, uma lua habitada pelos Na'vi. Usando seu avatar, Jake explora esse mundo incrível e se infiltra na tribo local. Ao se conectar com os Na'vi, começa a questionar a missão humana. Quando o conflito aumenta, ele precisa escolher seu lado.")
                        .time(162).genre(sciFi).classification(pg13)
                        .director(resolveDirector("James Cameron", directorCache))
                        .casting(resolveCasting(List.of("Sam Worthington", "Zoe Saldana"), actorCache))
                        .urlImageX("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823891/xqpfxmwdgdf2gd3eivpf.jpg")
                        .urlImageY("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823892/xns2q0rxbe1gazximlau.jpg")
                        .urlTrailer("https://www.youtube.com/watch?v=5PSNL1qE6VY")
                        .releaseDate(LocalDate.of(2009, 12, 18))
                        .isAvailable(true).isComingSoon(true)
                        .build(),

                Movie.builder()
                        .title("Coco").titleEn("Coco").titleFr("Coco").titleDe("Coco").titleIt("Coco").titlePt("Coco")
                        .synopsis("Miguel, un niño mexicano, sueña con convertirse en músico a pesar de la prohibición de su familia. Durante el Día de los Muertos, una serie de eventos mágicos lo transportan al colorido Mundo de los Muertos. Allí, conoce a sus antepasados y a un misterioso músico llamado Héctor. Miguel deberá descubrir los secretos de su linaje para poder regresar al mundo real y cumplir su sueño.")
                        .synopsisEn("Miguel, a young Mexican boy, dreams of becoming a musician despite his family's generations-old ban on music. On the Day of the Dead, a magical event transports him to the colorful Land of the Dead. There, he meets his ancestors and a mysterious trickster named Héctor. Miguel must uncover the truth about his family's past to return and pursue his passion.")
                        .synopsisFr("Miguel, un jeune garçon mexicain, rêve de devenir musicien, bien que sa famille interdise la musique depuis des générations. Lors du Jour des Morts, il est transporté dans le Monde des Morts. Il y rencontre ses ancêtres et un mystérieux personnage nommé Héctor. Pour revenir chez lui, Miguel doit découvrir les secrets de sa famille.")
                        .synopsisDe("Miguel, ein junger mexikanischer Junge, träumt davon, Musiker zu werden – obwohl seine Familie Musik seit Generationen verboten hat. Am Tag der Toten wird er ins bunte Reich der Toten versetzt. Dort trifft er auf seine Vorfahren und einen geheimnisvollen Mann namens Héctor. Miguel muss die Wahrheit über seine Familie erfahren, um zurückzukehren.")
                        .synopsisIt("Miguel, un giovane ragazzo messicano, sogna di diventare musicista nonostante il divieto imposto dalla sua famiglia. Durante il Día de los Muertos, viene trasportato nel vivace Mondo dei Morti. Qui incontra i suoi antenati e un uomo di nome Héctor. Miguel dovrà scoprire il passato della sua famiglia per poter tornare a casa.")
                        .synopsisPt("Miguel, um garoto mexicano, sonha em ser músico mesmo com a proibição da música em sua família. Durante o Dia dos Mortos, ele é magicamente levado para a Terra dos Mortos. Lá, conhece seus ancestrais e um homem chamado Héctor. Miguel precisa descobrir a verdade sobre sua família para voltar ao mundo dos vivos.")
                        .time(105).genre(animacion).classification(pg6)
                        .director(resolveDirector("Lee Unkrich", directorCache))
                        .casting(resolveCasting(List.of("Anthony Gonzalez", "Gael García Bernal"), actorCache))
                        .urlImageX("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823894/i0ziwxf5qyyg0n2qnprz.jpg")
                        .urlImageY("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823896/tsimxbj6jkbq2yosycnh.jpg")
                        .urlTrailer("https://www.youtube.com/watch?v=Rvr68u6k5sI")
                        .releaseDate(LocalDate.of(2017, 10, 20))
                        .isAvailable(true).isComingSoon(false)
                        .build(),

                Movie.builder()
                        .title("Inception").titleEn("Inception").titleFr("Inception").titleDe("Inception").titleIt("Inception").titlePt("A Origem")
                        .synopsis("Dom Cobb es un ladrón especializado en el espionaje corporativo mediante la infiltración en los sueños de sus objetivos. Cuando se le ofrece borrar su pasado criminal a cambio de realizar una última misión imposible: la 'origen', implantar una idea en lugar de robarla, acepta. Junto a su equipo, se adentra en varios niveles de sueños, enfrentando sus propios traumas y los límites de la percepción.")
                        .synopsisEn("Dom Cobb is a skilled thief, the best in the dangerous art of extraction: stealing secrets from deep within the subconscious during dreams. He's offered a chance to have his criminal history erased if he can accomplish the impossible—'inception': planting an idea. With a team of specialists, he dives through layered dreamscapes, facing personal demons and unraveling the fabric of reality.")
                        .synopsisFr("Dom Cobb est un voleur expérimenté, maître dans l'art dangereux de l'extraction d'informations au cœur des rêves. Pour effacer son passé criminel, on lui propose une mission: implanter une idée au lieu de la voler. Avec son équipe, il plonge dans les rêves imbriqués, affrontant ses démons et les frontières du réel.")
                        .synopsisDe("Dom Cobb ist ein erfahrener Dieb, der sich auf das Extrahieren von Informationen aus dem Unterbewusstsein während des Träumens spezialisiert hat. Um seine kriminelle Vergangenheit auszulöschen, muss er eine unmögliche Aufgabe erfüllen: die 'Inception', das Einpflanzen einer Idee. Zusammen mit seinem Team dringt er in verschachtelte Traumebenen vor und kämpft gegen innere Dämonen.")
                        .synopsisIt("Dom Cobb è un ladro esperto nell'arte dell'estrazione di segreti direttamente dal subconscio durante il sogno. Gli viene offerta una possibilità di redenzione se riuscirà a compiere l'impossibile: l'innesto di un'idea. Insieme al suo team, si addentra nei sogni più profondi, lottando con i propri demoni e i confini della realtà.")
                        .synopsisPt("Dom Cobb é um ladrão especializado em extrair segredos do subconsciente das pessoas enquanto elas sonham. Para limpar seu passado criminal, ele precisa realizar o impossível: plantar uma ideia em vez de roubá-la. Com sua equipe, mergulha em sonhos dentro de sonhos, enfrentando traumas e o colapso da realidade.")
                        .time(148).genre(fantasia).classification(pg18)
                        .director(resolveDirector("Christopher Nolan", directorCache))
                        .casting(resolveCasting(List.of("Leonardo DiCaprio", "Joseph Gordon-Levitt"), actorCache))
                        .urlImageX("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823897/xxzd4hakgp6ck6go7jcu.jpg")
                        .urlImageY("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823899/yzuhgqnr2knpjpaxpy0t.jpg")
                        .urlTrailer("https://www.youtube.com/watch?v=YoHD9XEInc0")
                        .releaseDate(LocalDate.of(2010, 7, 16))
                        .isAvailable(true).isComingSoon(false)
                        .build(),

                Movie.builder()
                .title("Capitán América: Un Nuevo Mundo")
                .titleEn("Captain America: Brave New World")
                .titleFr("Captain America: Nouveau Monde")
                .titleDe("Captain America: Neue Welt")
                .titleIt("Captain America: Nuovo Mondo")
                .titlePt("Capitão América: Novo Mundo")
                .synopsis("Sam Wilson asume el manto de Capitán América tras la muerte de Steve Rogers. Enfrentándose a una nueva amenaza internacional, debe navegar por la política global y proteger a su país mientras lidia con su nueva identidad.")
                .synopsisEn("Sam Wilson takes up the mantle of Captain America following Steve Rogers' death. Facing a new international threat, he must navigate global politics and protect his country while grappling with his new identity.")
                .synopsisFr("Sam Wilson prend le manteau de Captain America après la mort de Steve Rogers. Confronté à une nouvelle menace internationale, il doit naviguer dans la politique mondiale et protéger son pays tout en luttant avec sa nouvelle identité.")
                .synopsisDe("Sam Wilson übernimmt das Erbe von Captain America nach dem Tod von Steve Rogers. Mit einer neuen internationalen Bedrohung konfrontiert, muss er sich durch die globale Politik navigieren und sein Land schützen, während er mit seiner neuen Identität kämpft.")
                .synopsisIt("Sam Wilson assume il manto di Capitano America dopo la morte di Steve Rogers. Affrontando una nuova minaccia internazionale, deve navigare nella politica globale e proteggere il suo paese mentre lotta con la sua nuova identità.")
                .synopsisPt("Sam Wilson assume o manto de Capitão América após a morte de Steve Rogers. Enfrentando uma nova ameaça internacional, ele deve navegar pela política global e proteger seu país enquanto lida com sua nova identidade.")
                .time(120)
                .genre(accion)
                .classification(pg13)
                .director(resolveDirector("Jake Schreier", directorCache))
                .casting(resolveCasting(List.of("Anthony Mackie", "Olivia Colman", "Daniel Brühl"), actorCache))
                .urlImageX("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823900/vp5mh1c0xnh254xocv9z.jpg")
                .urlImageY("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823902/x2l3friz7equmo9sqgje.jpg")
                .urlTrailer("https://www.youtube.com/watch?v=S1rK531kI_8")
                .releaseDate(LocalDate.of(2025, 4, 30))
                .isAvailable(true)
                .isComingSoon(false)
                .build(),

                Movie.builder()
                        .title("Guardianes de la Galaxia: Volumen 3")
                        .titleEn("Guardians of the Galaxy Vol. 3")
                        .titleFr("Les Gardiens de la Galaxie Vol. 3")
                        .titleDe("Guardians of the Galaxy Vol. 3")
                        .titleIt("Guardiani della Galassia Vol. 3")
                        .titlePt("Guardiões da Galáxia Vol. 3")
                        .synopsis("Los Guardianes de la Galaxia enfrentan una nueva amenaza que pondrá a prueba su unidad y su lealtad. Star-Lord lidera a sus amigos en una aventura para salvar el universo una vez más.")
                        .synopsisEn("The Guardians of the Galaxy face a new threat that will test their unity and loyalty. Star-Lord leads his friends on an adventure to save the universe once again.")
                        .synopsisFr("Les Gardiens de la Galaxie affrontent une nouvelle menace qui mettra à l'épreuve leur unité et leur loyauté. Star-Lord mène ses amis dans une aventure pour sauver l'univers une fois de plus.")
                        .synopsisDe("Die Wächter der Galaxis stehen einer neuen Bedrohung gegenüber, die ihre Einheit und Loyalität auf die Probe stellen wird. Star-Lord führt seine Freunde auf ein Abenteuer, um das Universum erneut zu retten.")
                        .synopsisIt("I Guardiani della Galassia affrontano una nuova minaccia che metterà alla prova la loro unità e lealtà. Star-Lord guida i suoi amici in un'avventura per salvare l'universo ancora una volta.")
                        .synopsisPt("Os Guardiões da Galáxia enfrentam uma nova ameaça que colocará à prova sua unidade e lealdade. Star-Lord lidera seus amigos em uma aventura para salvar o universo mais uma vez.")
                        .time(150)
                        .genre(drama)
                        .classification(pg13)
                        .director(resolveDirector("James Gunn", directorCache))
                        .casting(resolveCasting(List.of("Chris Pratt", "Zoe Saldana", "Dave Bautista"), actorCache))
                        .urlImageX("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823904/rexltxvt6x2jghxpb9gs.jpg")
                        .urlImageY("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823906/qtpkmizpbzihlk2bfw5k.jpg")
                        .urlTrailer("https://youtu.be/9SfnkovRye8?si=E67ZnMMfyVIU3iyz")
                        .releaseDate(LocalDate.of(2025, 5, 10))
                        .isAvailable(true)
                        .isComingSoon(false)
                        .build(),

                Movie.builder()
                        .title("Flash")
                        .titleEn("The Flash")
                        .titleFr("Flash")
                        .titleDe("The Flash")
                        .titleIt("Flash")
                        .titlePt("Flash")
                        .synopsis("Barry Allen lucha por controlar sus poderes y enfrentarse a una amenaza de otro universo que pone en peligro la existencia misma del tiempo.")
                        .synopsisEn("Barry Allen struggles to control his powers and faces a threat from another universe that jeopardizes the very fabric of time.")
                        .synopsisFr("Barry Allen lutte pour contrôler ses pouvoirs et affronte une menace d'un autre univers qui met en danger le tissu même du temps.")
                        .synopsisDe("Barry Allen kämpft darum, seine Kräfte zu kontrollieren und sich einer Bedrohung aus einem anderen Universum zu stellen, die das Gewebe der Zeit selbst gefährdet.")
                        .synopsisIt("Barry Allen lotta per controllare i suoi poteri e affronta una minaccia proveniente da un altro universo che mette in pericolo la stessa esistenza del tempo.")
                        .synopsisPt("Barry Allen luta para controlar seus poderes e enfrenta uma ameaça de outro universo que coloca em risco a própria existência do tempo.")
                        .time(130)
                        .genre(suspenso)
                        .classification(pg0)
                        .director(resolveDirector("John Francis Daley", directorCache))
                        .casting(resolveCasting(List.of("Ezra Miller", "Michael Keaton", "Ben Affleck"), actorCache))
                        .urlImageX("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823907/mcla9u1th09dgjsarddj.png")
                        .urlImageY("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823912/gmwk7cb5fwuwszbmliub.jpg")
                        .urlTrailer("https://www.youtube.com/watch?v=gun18R-zXVU")
                        .releaseDate(LocalDate.of(2025, 6, 15))
                        .isAvailable(true)
                        .isComingSoon(true)
                        .build(),

                Movie.builder()
                        .title("The Marvels")
                        .titleEn("The Marvels")
                        .titleFr("Les Marvels")
                        .titleDe("Die Marvels")
                        .titleIt("Le Marvels")
                        .titlePt("Os Marvels")
                        .synopsis("Carol Danvers, Kamala Khan y Monica Rambeau se unen para enfrentar una amenaza cósmica que amenaza la estabilidad del universo.")
                        .synopsisEn("Carol Danvers, Kamala Khan, and Monica Rambeau team up to face a cosmic threat that threatens the stability of the universe.")
                        .synopsisFr("Carol Danvers, Kamala Khan et Monica Rambeau s'unissent pour affronter une menace cosmique qui menace la stabilité de l'univers.")
                        .synopsisDe("Carol Danvers, Kamala Khan und Monica Rambeau schließen sich zusammen, um einer kosmischen Bedrohung zu begegnen, die die Stabilität des Universums gefährdet.")
                        .synopsisIt("Carol Danvers, Kamala Khan e Monica Rambeau si uniscono per affrontare una minaccia cosmica che minaccia la stabilità dell'universo.")
                        .synopsisPt("Carol Danvers, Kamala Khan e Monica Rambeau se unem para enfrentar uma ameaça cósmica que ameaça a estabilidade do universo.")
                        .time(140)
                        .genre(terror)
                        .classification(pg16)
                        .director(resolveDirector("Nia DaCosta", directorCache))
                        .casting(resolveCasting(List.of("Brie Larson", "Iman Vellani", "Teyonah Parris"), actorCache))
                        .urlImageX("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823913/agbcnmdk90m5purwhsa7.jpg")
                        .urlImageY("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745920554/aiowce4kdfr9fen4tqnx.webp")
                        .urlTrailer("https://www.youtube.com/watch?v=gdSGIf8kbhg")
                        .releaseDate(LocalDate.of(2025, 7, 20))
                        .isAvailable(true)
                        .isComingSoon(true)
                        .build(),

                Movie.builder()
                        .title("Deadpool 3")
                        .titleEn("Deadpool 3")
                        .titleFr("Deadpool 3")
                        .titleDe("Deadpool 3")
                        .titleIt("Deadpool 3")
                        .titlePt("Deadpool 3")
                        .synopsis("Deadpool se enfrenta a una nueva amenaza cuando se ve involucrado en un viaje en el tiempo que pondrá en peligro tanto su vida como la de sus seres queridos.")
                        .synopsisEn("Deadpool faces a new threat as he gets involved in a time-travel adventure that jeopardizes both his life and those of his loved ones.")
                        .synopsisFr("Deadpool fait face à une nouvelle menace lorsqu'il se retrouve impliqué dans une aventure de voyage dans le temps qui met en danger sa vie et celle de ses proches.")
                        .synopsisDe("Deadpool sieht sich einer neuen Bedrohung gegenüber, als er in ein Zeitreise-Abenteuer verwickelt wird, das sowohl sein Leben als auch das seiner Liebsten gefährdet.")
                        .synopsisIt("Deadpool affronta una nuova minaccia quando si trova coinvolto in un'avventura nel tempo che mette a rischio sia la sua vita che quella dei suoi cari.")
                        .synopsisPt("Deadpool enfrenta uma nova ameaça quando se vê envolvido em uma aventura de viagem no tempo que coloca em risco sua vida e a de seus entes queridos.")
                        .time(125)
                        .genre(comedia)
                        .classification(pg6)
                        .director(resolveDirector("Shawn Levy", directorCache))
                        .casting(resolveCasting(List.of("Ryan Reynolds", "Hugh Jackman", "Morena Baccarin"), actorCache))
                        .urlImageX("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823917/ojyenz0mhutkor3oazgt.jpg")
                        .urlImageY("https://res.cloudinary.com/dsby8nzqq/image/upload/v1745823919/bf3jcq6hisc3e9pj5xht.jpg")
                        .urlTrailer("https://www.youtube.com/watch?v=tTM5weeCFvQ&t=2s")
                        .releaseDate(LocalDate.of(2025, 8, 5))
                        .isAvailable(true)
                        .isComingSoon(true)
                        .build()
                );

        movieRepository.saveAll(movies);
        logger.info("Películas insertadas con soporte multilingüe");
    }

    /* Helpers: evitan duplicados de Director / Actor y simplifican el builder    */
    private Director resolveDirector(String name, Map<String, Director> cache) {
        return cache.computeIfAbsent(name,
                n -> directorRepository.findByName(n)
                        .orElseGet(() -> directorRepository.save(Director.builder().name(n).build())));
    }

    private List<Actor> resolveCasting(List<String> names, Map<String, Actor> cache) {
        return names.stream()
                .map(n -> cache.computeIfAbsent(n,
                        a -> actorRepository.findByNameIgnoreCase(a)
                                .orElseGet(() -> actorRepository.save(Actor.builder().name(a).build()))))
                .toList();
    }

    private void insertarRegiones() {
        if (regionRepository.count() == 0) {
            regionRepository.saveAll(
                    List.of("La Laguna", "La Orotava", "Santa Cruz de Tenerife")
                            .stream()
                            .map(n -> Region.builder().name(n).build())
                            .toList()
            );
            logger.info("Regiones insertadas");
        }
    }

    private record RoomDef(String nombre, int filas, int columnas) {}
    private static final List<RoomDef> ROOMS_LA_LAGUNA = List.of(
            new RoomDef("Laguna Sala 1", 9, 12),   // 9×12=108
            new RoomDef("Laguna Sala 2", 8, 10));  // 80
    private static final List<RoomDef> ROOMS_LA_OROTAVA = List.of(
            new RoomDef("Orotava Sala 1", 10, 14));                       // 140
    private static final List<RoomDef> ROOMS_SANTA_CRUZ = List.of(
            new RoomDef("SC Sala 1",  9, 12),
            new RoomDef("SC Sala 2", 12, 16),    // sala grande 192 butaca
            new RoomDef("SC Sala 3", 6,  8));     // minisala 48

    private void insertarSalasYAsientos(RoomRepository roomRepo, AsientoRepository asientoRepo) {

        if (roomRepo.count() > 0 || asientoRepo.count() > 0) return;

        /* 1️⃣  Obtener las regiones ya insertadas */
        Region laLaguna   = regionRepository.findByNameIgnoreCase("La Laguna").orElseThrow();
        Region laOrotava  = regionRepository.findByNameIgnoreCase("La Orotava").orElseThrow();
        Region santaCruz  = regionRepository.findByNameIgnoreCase("Santa Cruz de Tenerife").orElseThrow();

        /* 2️⃣  Crear y guardar cada sala con sus asientos */
        crearSalasParaRegion(laLaguna,   ROOMS_LA_LAGUNA,  roomRepo);
        crearSalasParaRegion(laOrotava,  ROOMS_LA_OROTAVA, roomRepo);
        crearSalasParaRegion(santaCruz,  ROOMS_SANTA_CRUZ, roomRepo);

        logger.info("Rooms y asientos insertados, cada una ligada a su región.");
    }

    /* Helper que genera todas las salas de una región */
    private void crearSalasParaRegion(Region region, List<RoomDef> defs, RoomRepository roomRepo) {

        for (RoomDef def : defs) {
            Room room = Room.builder()
                    .nombreroom(def.nombre)
                    .filas(def.filas)
                    .columnas(def.columnas)
                    .capacidad(def.filas * def.columnas)
                    .region(region)
                    .build();

            /*  Generar asientos */
            char filaFin = (char) ('A' + def.filas - 1);
            for (char fila = 'A'; fila <= filaFin; fila++) {
                for (int col = 1; col <= def.columnas; col++) {
                    Asiento asiento = Asiento.builder()
                            .fila(String.valueOf(fila))
                            .columna(col)
                            .tipoAsiento(tipoAsientoPorFila(fila))
                            .disponible(true)
                            .room(room)
                            .build();
                    room.agregarAsiento(asiento);
                }
            }
            roomRepo.save(room);   // cascade persistirá los asientos
        }
    }

    /*  VIP para la fila B, minusválido para la fila A, resto normal */
    private String tipoAsientoPorFila(char fila) {
        return (fila == 'A') ? "minusvalido" :
                (fila == 'B') ? "VIP" : "normal";
    }



    // Favoritos
    private void insertarFavoritos(FavoriteRepository favoriteRepository, UserRepository userRepository, MovieRepository movieRepository) {
        if (favoriteRepository.count() == 0) {
            List<User> users = userRepository.findAll();
            List<Movie> movies = movieRepository.findAll();

            favoriteRepository.saveAll(List.of(
                    Favorite.builder().user(users.get(0)).movie(movies.get(0)).addedDate(LocalDateTime.now()).build(),
                    Favorite.builder().user(users.get(0)).movie(movies.get(1)).addedDate(LocalDateTime.now()).build()
            ));
            logger.info("Favoritos insertados");
        }
    }

    // Emisiones
    private static final String IDIOMA_ESP_SUB = "Español subtitulado";
    private static final String IDIOMA_ESP_DOB = "Español doblado";
    private static final String IDIOMA_ENG_SUB = "Inglés subtitulado";
    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;
    private final ClassificationRepository classificationRepository;
    private final DirectorRepository directorRepository;
    private final ActorRepository actorRepository;
    private final RegionRepository regionRepository;

    public DataSeeder(CloudinaryService cloudinaryService, GenreRepository genreRepository,
                      MovieRepository movieRepository,
                      ClassificationRepository classificationRepository,
                      DirectorRepository directorRepository,
                      ActorRepository actorRepository,
                      RegionRepository regionRepository) {
        this.cloudinaryService = cloudinaryService;
        this.genreRepository = genreRepository;
        this.movieRepository = movieRepository;
        this.classificationRepository = classificationRepository;
        this.directorRepository = directorRepository;
        this.actorRepository = actorRepository;
        this.regionRepository = regionRepository;
    }


    private void insertarTickets(TicketRepository ticketRepository) {
        if (ticketRepository.count() == 0) {
            ticketRepository.saveAll(List.of(
                    Ticket.builder().name("Adulto").ticketPrice(10.90).build(),
                    Ticket.builder().name("Nino").ticketPrice(5.90).build(),
                    Ticket.builder().name("VIP").ticketPrice(15.90).build(),
                    Ticket.builder().name("Reducido").ticketPrice(8.90).build()
            ));
            logger.info("Tickets insertados");
        }
    }

    private void insertarReservas(ReservationRepository reservationRepository, EmisionRepository emisionRepository, UserRepository userRepository, TicketRepository ticketRepository) {
        if (reservationRepository.count() > 0) return;

        List<User> usuarios = userRepository.findAll();
        Ticket ticketAdulto = ticketRepository.findByName("Adulto").orElseThrow();

        // Buscar emisiones de Titanic (ocupación alta)
        List<Emision> emisionesTitanic = emisionRepository.findByMovie_TitleIgnoreCase("Titanic");
        // Buscar emisiones de Coco (ocupación baja)
        List<Emision> emisionesCoco = emisionRepository.findByMovie_TitleIgnoreCase("Coco");

        // Titanic: llenar el 80% de las butacas
        for (Emision emision : emisionesTitanic) {
            int capacidad = emision.getRoom().getCapacidad();
            int reservasNecesarias = (int) (capacidad * 0.8); // 80%

            for (int i = 0; i < reservasNecesarias; i++) {
                Reservation reserva = Reservation.builder()
                        .user(usuarios.get(i % usuarios.size()))
                        .emision(emision)
                        .reservationDate(LocalDateTime.now())
                        .status("confirmed")
                        .totalPrice(10.90)
                        .build();
                reservationRepository.save(reserva);
            }
        }

        // Coco: llenar el 30% de las butacas
        for (Emision emision : emisionesCoco) {
            int capacidad = emision.getRoom().getCapacidad();
            int reservasNecesarias = (int) (capacidad * 0.3); // 30%

            for (int i = 0; i < reservasNecesarias; i++) {
                Reservation reserva = Reservation.builder()
                        .user(usuarios.get(i % usuarios.size()))
                        .emision(emision)
                        .reservationDate(LocalDateTime.now())
                        .status("confirmed")
                        .totalPrice(10.90)
                        .build();
                reservationRepository.save(reserva);
            }
        }

        logger.info("Reservas insertadas: Titanic (80%), Coco (30%)");
    }



}
