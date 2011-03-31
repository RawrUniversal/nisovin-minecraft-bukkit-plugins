package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerChatEvent;

public class Packet20NamedEntitySpawn extends Packet {

    public int a;
    public String b;
    public int c;
    public int d;
    public int e;
    public byte f;
    public byte g;
    public int h;

    public Packet20NamedEntitySpawn() {}

    public Packet20NamedEntitySpawn(EntityHuman entityhuman) {
        this.a = entityhuman.id;
        try {
	        if (entityhuman instanceof EntityPlayer) {
	        	EntityPlayer player = (EntityPlayer) entityhuman;
	        	String comm = "_nameplate_color_check";
	        	PlayerChatEvent event = new PlayerChatEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, new CraftPlayer(player.b.server, player), comm);
	        	player.b.server.getPluginManager().callEvent(event);
	        	if (event.getMessage().startsWith("_nameplate_color_set_")) {
	        		int color = Integer.parseInt(event.getMessage().substring(21));
	        		this.b = ChatColor.getByCode(color) + entityhuman.name;
	        	} else {
	        		this.b = entityhuman.name;
	        	}
	        	//this.b = ((EntityPlayer)entityhuman).displayName;
	        } else {
	        	this.b = entityhuman.name;
	        }
        } catch (Exception e) {
        	this.b = entityhuman.name;
        }
        this.c = MathHelper.b(entityhuman.locX * 32.0D);
        this.d = MathHelper.b(entityhuman.locY * 32.0D);
        this.e = MathHelper.b(entityhuman.locZ * 32.0D);
        this.f = (byte) ((int) (entityhuman.yaw * 256.0F / 360.0F));
        this.g = (byte) ((int) (entityhuman.pitch * 256.0F / 360.0F));
        ItemStack itemstack = entityhuman.inventory.b();

        this.h = itemstack == null ? 0 : itemstack.id;
    }

    public void a(DataInputStream datainputstream) {
    	try {
        this.a = datainputstream.readInt();
        this.b = datainputstream.readUTF();
        this.c = datainputstream.readInt();
        this.d = datainputstream.readInt();
        this.e = datainputstream.readInt();
        this.f = datainputstream.readByte();
        this.g = datainputstream.readByte();
        this.h = datainputstream.readShort();
    	} catch (IOException e) {
    	}
    }

    public void a(DataOutputStream dataoutputstream) {
    	try {
        dataoutputstream.writeInt(this.a);
        dataoutputstream.writeUTF(this.b);
        dataoutputstream.writeInt(this.c);
        dataoutputstream.writeInt(this.d);
        dataoutputstream.writeInt(this.e);
        dataoutputstream.writeByte(this.f);
        dataoutputstream.writeByte(this.g);
        dataoutputstream.writeShort(this.h);
    	} catch (IOException e) {
    	}
    }

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public int a() {
        return 28;
    }
}
