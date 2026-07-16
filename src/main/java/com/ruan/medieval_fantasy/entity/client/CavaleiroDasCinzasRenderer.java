package com.ruan.medieval_fantasy.entity.client;

import com.ruan.medieval_fantasy.entity.custom.CavaleiroDasCinzas;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CavaleiroDasCinzasRenderer extends GeoEntityRenderer<CavaleiroDasCinzas> {

    public CavaleiroDasCinzasRenderer(EntityRendererProvider.Context context) {
        super(context, new CavaleiroDasCinzasModel());
        this.shadowRadius = 1.05F;
    }
}
