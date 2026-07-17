package com.ruan.medieval_fantasy.origin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class OriginRegistry {

    private static final Map<OriginType, OriginData> ORIGINS = new LinkedHashMap<>();

    static {
        register(new OriginData(
                OriginType.KNIGHT,
                "Cavaleiro",
                "Honra, juramento e tradicao",
                "Criado nas antigas tradicoes dos reinos. Nobres, guardioes e cavaleiros podem reconhecer sua autoridade.",
                List.of(
                        "Dialogos raros sobre brasoes, honra e juramentos.",
                        "Alguns conflitos podem ser resolvidos por tradicao.",
                        "Certas reliquias podem reconhecer sangue ou juramento."
                ),
                Map.of("nobles", 2, "guards", 1, "criminals", -1)
        ));
        register(new OriginData(
                OriginType.ARCHAEOLOGIST,
                "Arqueologo",
                "Conhecimento, ruinas e reliquias",
                "Estudioso das civilizacoes antigas. Enxerga historia onde outros veem apenas pedra quebrada.",
                List.of(
                        "Dialogos raros sobre simbolos, inscricoes e guerras antigas.",
                        "Pode descobrir pistas ocultas em puzzles e eventos.",
                        "Identifica origem e contexto de reliquias."
                ),
                Map.of("scholars", 2, "nobles", 1)
        ));
        register(new OriginData(
                OriginType.RENEGADE,
                "Renegado",
                "Liberdade, blefe e sobrevivencia",
                "Viveu fora da ordem dos reinos. Sabe intimidar, mentir e negociar com gente que prefere sombras.",
                List.of(
                        "Dialogos raros de intimidacao, blefe e provocacao.",
                        "Criminosos e mercadores ilegais confiam mais.",
                        "Nobres e guardas tendem a desconfiar."
                ),
                Map.of("criminals", 2, "guards", -1, "nobles", -2)
        ));
    }

    private OriginRegistry() {
    }

    public static void register(OriginData data) {
        ORIGINS.put(data.getType(), data);
    }

    public static OriginData get(OriginType type) {
        return ORIGINS.get(type);
    }

    public static boolean isSelectable(OriginType type) {
        return type != null && type != OriginType.NONE && ORIGINS.containsKey(type);
    }

    public static Collection<OriginData> all() {
        return ORIGINS.values();
    }
}
