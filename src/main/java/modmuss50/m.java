package modmuss50;

import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;

@Mod(modid = "16k", name = "16k", version = "0.16")
public class m {

	@Mod.EventHandler
	private void pi(FMLPreInitializationEvent e) {
		config = new Configuration(e.getSuggestedConfigurationFile());
		m.value = config.getFloat("Scale", "MapSettings", 0.5F, 0F, 100F, "This can be changed ingame!");

		if (config.hasChanged()) {
			config.save();
		}
	}

	@Mod.EventHandler
	private void i(FMLInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
	}

	int lastx = 0;
	int lastz = 0;
	public static float value = 0.5F;
	public static Configuration config;

	@SubscribeEvent
	public void r(RenderGameOverlayEvent e) {
		final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (lastx != (int) player.posX || lastz != (int) player.posZ) {
			computeMap(64, 64, player);
			lastx = (int) player.posX;
			lastz = (int) player.posZ;
		}
		GL11.glPushMatrix();
		GL11.glScalef(m.value * 2, m.value * 2, m.value * 2);
		GL11.glTranslated(-10 / 2, -10 / 2, 0);
		GL11.glTranslated(10, 10, 0);
		mapTexture.drawMap(0, 0, 300F);
		GL11.glScalef(1F, 1F, 1F);
		GL11.glPopMatrix();
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

	public void setColor(int x, int y, double r, double g, double b, double a) {
		int i = (int) (a * 255.0F);
		int j = (int) (r * 255.0F);
		int k = (int) (g * 255.0F);
		int l = (int) (b * 255.0F);
		colorMap[x + y * width] = i << 24 | j << 16 | k << 8 | l;
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
		this.buttonList.add(new GuiButton(1, this.width / 2 - 35, this.height - 38, 70, 20, I18n.format("gui.done")));
		this.buttonList.add(new slider(2, this.width / 2 - 75, 70, 150));
	}

	@Override
	public void actionPerformed(GuiButton button) {
		if (button.enabled && button.id == 1) {
			m.config.get("MapSettings", "Scale", "0.5", "This can be changed ingame!").set(m.value);
			m.config.save();
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

@SideOnly(Side.CLIENT)
class slider extends GuiButton {

	public boolean isMouseDown;
	private static final String __OBFID = "CL_00000717";

	public slider(int buttonid, int x, int y, int length) {
		super(buttonid, x, y, length, 20, "");
		this.displayString = "Scale : " + m.value * 100;
	}

	/**
	 * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering
	 * over this button.
	 */
	protected int getHoverState(boolean mouseOver) {
		return 0;
	}

	/**
	 * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
	 */
	protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			if (this.isMouseDown) {
				m.value = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
				m.value = MathHelper.clamp_float(m.value, 0.0F, 1.0F);
				this.displayString = "Scale : " + m.value * 100;
			}

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.drawTexturedModalRect(this.xPosition + (int) (m.value * (float) (this.width - 8)), this.yPosition, 0, 66, 4, 20);
			this.drawTexturedModalRect(this.xPosition + (int) (m.value * (float) (this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
		}
	}

	/**
	 * Returns true if the mouse has been pressed on this control. Equivalent of
	 * MouseListener.mousePressed(MouseEvent e).
	 */
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			m.value = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
			m.value = MathHelper.clamp_float(m.value, 0.0F, 1.0F);
			this.displayString = "Scale : " + m.value * 100;
			this.isMouseDown = true;
			return true;
		} else {
			return false;
		}
	}

	public void playPressSound(SoundHandler soundHandlerIn) {
	}

	/**
	 * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
	 */
	public void mouseReleased(int mouseX, int mouseY) {
		this.isMouseDown = false;
	}
}

