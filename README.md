# user-service


ovo je user-service microservis repo
Razdivjen je jer korsitimo najbolje microservis prakse i lakse skaliranje i odvajanje razvoja te odvojeni db

ovaj servis ce biti odgvoran za security korsinika applikacija putem jwt i oauth2


 -> Korisiti cu shared lib module za kesiranje jer imam dosta klasa sto se tice localno i dostrubutivnog kesiranja (caffeince i redis) te najbolja praksa je
    onda kreirati zajedni "shared library" s kojim cu to dijeliti kao modul jer i user service i course service ce mi trebati imati i second level caching

- imati cu svaki manji compose yml file za svaki servis i jedan veliki koji ce spajati ta dva 

 dodao local repository kao mavenlocal za za standalone library za kesiranje koje cu korsiti u ovom servisu 