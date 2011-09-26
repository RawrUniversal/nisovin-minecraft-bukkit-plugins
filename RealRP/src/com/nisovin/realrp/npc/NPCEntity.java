package com.nisovin.realrp.npc;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet18ArmAnimation;
import net.minecraft.server.World;
import net.minecraft.server.WorldServer;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.entity.HumanEntity;

/**
*
* @author martin
*/
public class NPCEntity extends EntityPlayer {

    public NPCEntity(MinecraftServer minecraftserver, World world, String s, ItemInWorldManager iteminworldmanager) {
        super(minecraftserver, world, s, iteminworldmanager);
        NetworkManager netMgr = new NPCNetworkManager(new NullSocket(), "NPC Manager", new NetHandler() {

            @Override
            public boolean c() {
                return true;
            }
        });
        this.netServerHandler = new NPCNetHandler(minecraftserver, netMgr, this);
    }

    public void animateArmSwing() {
        ((WorldServer)this.world).tracker.a(this, new Packet18ArmAnimation(this, 1));
    }

    public void actAsHurt() {
        ((WorldServer)this.world).tracker.a(this, new Packet18ArmAnimation(this, 2));
    }

    public PlayerInventory getInventory() {
     return ((HumanEntity) getBukkitEntity()).getInventory();
    }

    public void setItemInHand(ItemStack item) {
        ((HumanEntity) getBukkitEntity()).setItemInHand(item);
    }
    
    @Override
    public void move(double arg0, double arg1, double arg2) {
     setPosition(arg0, arg1, arg2);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}