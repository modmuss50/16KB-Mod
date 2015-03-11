package modmuss50;

import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

@Mod(modid = "16k", name = "16k", version = "0.16")
public class m {

	@Mod.EventHandler
	private void i(FMLInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
	}

	int lastx = 0;
	int lastz = 0;

	@SubscribeEvent
	public void r(RenderGameOverlayEvent e) {
		final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (lastx != (int) player.posX || lastz != (int) player.posZ) {
			computeMap(64, 64, player);
			lastx = (int) player.posX;
			lastz = (int) player.posZ;
		}
		mapTexture.drawMap(10, 10, 300F);
	}

	@SubscribeEvent()
	public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post evt) {
		if (evt.gui instanceof GuiOptions) {
			List<GuiButton> buttonList = evt.buttonList;
			GuiButton button = new GuiButton(405, 1, 1, 45, 20, "16k map");
			buttonList.add(button);
		}
	}

	@SubscribeEvent
	public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent evt) {
		if (evt.gui instanceof GuiOptions) {
			if (evt.button.id == 405) {
				FMLClientHandler.instance().showGuiScreen(new gui(evt.gui));
			}
		}
	}

	public ct mapTexture = new ct(64, 64);

	private void computeMap(int width, int height, EntityPlayer player) {
		int zoom = 1;
		int startHight = 256;
		final int[] textureData = new int[width * height];
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				int x = i + (int) player.posX - height / 2 * zoom;
				int z = j + (int) player.posZ - width / 2 * zoom;
				int y;
				for (y = startHight; y >= 0; y = y - 1) {
					if (player.worldObj.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.air) {
						startHight = y + 30;
						break;
					}
				}
				int color = Minecraft.getMinecraft().thePlayer.worldObj.getBlockState(new BlockPos(x, y, z)).getBlock().getMapColor(Minecraft.getMinecraft().thePlayer.worldObj.getBlockState(new BlockPos(x, y, z))).colorIndex;
				textureData[i + j] = color;
				mapTexture.setColor(i, j, MapColor.mapColorArray[color].colorValue);
			}
		}
		mapTexture.setColor(height / 2, width / 2, MapColor.redColor.colorValue);
		mapTexture.setColor(height / 2 + 1, width / 2, MapColor.redColor.colorValue);
		mapTexture.setColor(height / 2, width / 2 + 1, MapColor.redColor.colorValue);
		mapTexture.setColor(height / 2 + 1, width / 2 + 1, MapColor.redColor.colorValue);
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

class gui extends GuiScreen {
	private GuiScreen parent;

	public gui(GuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(1, this.width / 2 - 75, this.height - 38, I18n.format("gui.done")));
	}

	@Override
	public void actionPerformed(GuiButton button) {
		if (button.enabled && button.id == 1) {
			FMLClientHandler.instance().showGuiScreen(parent);
		}
	}

	@Override
	public void drawScreen(int x, int y, float f) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj, "16k map settings", this.width / 2, 40, 0xFFFFFF);
		super.drawScreen(x, y, f);
	}

}

