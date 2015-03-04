package modmuss50;

import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "16k", name = "16k", version = "0.16")
public class m {

	@Mod.EventHandler
	private void i(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void r(RenderGameOverlayEvent event){
		final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		computeMap(64, 64, player);
		mapTexture.drawMap(10, 10, 300F);
	}

	public ct mapTexture = new ct(64, 64);

	private void computeMap(int width, int height, EntityPlayer player) {
		int zoom = 1;
		final int[] textureData = new int[width * height];
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				int x = i + (int) player.posX - height /2 * zoom;
				int z = j + (int) player.posZ - width / 2 * zoom;
				int y;
				int startHight = 256;
				for (y = startHight; y >= 0; y = y - 1) {
					if (player.worldObj.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.air) {
						break;
					}
				}
				int color = Minecraft.getMinecraft().thePlayer.worldObj.getBlockState(new BlockPos(x, y, z)).getBlock().getMapColor(Minecraft.getMinecraft().thePlayer.worldObj.getBlockState(new BlockPos(x, y, z))).colorIndex;
				textureData[i + j] = color;
				mapTexture.setColor(i , j , MapColor.mapColorArray[color].colorValue);
			}
		}
		mapTexture.setColor( height /2 , width /2 , MapColor.redColor.colorValue);
		mapTexture.setColor( height /2 + 1, width /2 , MapColor.redColor.colorValue);
		mapTexture.setColor( height /2 , width /2 + 1, MapColor.redColor.colorValue);
		mapTexture.setColor( height /2  + 1, width /2  + 1, MapColor.redColor.colorValue);
	}
}

class ct {
	private int width, height;
	private int[] colorMap;

	private DynamicTexture dynamicTexture;

	public ct(int iWidth, int iHeight) {
		width = iWidth;
		height = iHeight;
		colorMap = new int[iWidth * iHeight];
		dynamicTexture = new DynamicTexture(width, height);
		colorMap = dynamicTexture.getTextureData();
	}

	public void setColor(int x, int y, int color) {
		colorMap[x + y * height] = 255 << 24 | color;
	}

	public void drawMap(int screenX, int screenY, float zLevel) {
		drawMap(screenX, screenY, zLevel, 0, 0, width, height);
	}

	public void drawMap(int screenX, int screenY, float zLevel, int clipX, int clipY, int clipWidth, int clipHeight) {
		dynamicTexture.updateDynamicTexture();
		float f = 1F / width;
		float f1 = 1F / height;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		worldRenderer.startDrawingQuads();
		worldRenderer.addVertexWithUV(screenX + 0, screenY + clipHeight, zLevel, (clipX + 0) * f, (clipY + clipHeight) * f1);
		worldRenderer.addVertexWithUV(screenX + clipWidth, screenY + clipHeight, zLevel, (clipX + clipWidth) * f, (clipY + clipHeight) * f1);
		worldRenderer.addVertexWithUV(screenX + clipWidth, screenY + 0, zLevel, (clipX + clipWidth) * f, (clipY + 0) * f1);
		worldRenderer.addVertexWithUV(screenX + 0, screenY + 0, zLevel, (clipX + 0) * f, (clipY + 0) * f1);
		tessellator.draw();
	}
}

