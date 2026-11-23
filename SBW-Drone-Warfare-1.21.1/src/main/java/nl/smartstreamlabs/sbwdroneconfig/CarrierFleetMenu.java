package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Read-only roster page: only a server-validated button can change the pilot target. */
public final class CarrierFleetMenu extends AbstractContainerMenu {
    public static final int PAGE_SIZE=5;
    public record Row(UUID id,String name,int number){}
    public final UUID home;public final int page,total;
    public final String leaving;
    public final List<Row> rows;
    private final ServerPlayer owner;
    private final ContainerData data;
    private long lastRefresh=-1;
    public CarrierFleetMenu(int id,Inventory inv,FriendlyByteBuf buf){
        super(AddonMenus.CARRIER_FLEET.get(),id);owner=null;home=buf.readUUID();page=buf.readVarInt();total=buf.readVarInt();leaving=buf.readUtf(256);
        int n=buf.readVarInt();if(n<1||n>PAGE_SIZE+1)throw new IllegalArgumentException("Invalid fleet page");var list=new ArrayList<Row>();for(int i=0;i<n;i++)list.add(new Row(buf.readUUID(),buf.readUtf(128),buf.readVarInt()));rows=List.copyOf(list);data=new SimpleContainerData(n*5);addDataSlots(data);
    }
    private CarrierFleetMenu(int id,Inventory inv,ServerPlayer p,UUID home,int page,int total,List<Row> rows){
        super(AddonMenus.CARRIER_FLEET.get(),id);this.owner=p;this.home=home;this.page=page;this.total=total;this.rows=List.copyOf(rows);leaving=CarrierFleet.leaving(p);data=new SimpleContainerData(rows.size()*5);refresh();addDataSlots(data);
    }
    public static void open(ServerPlayer p,UUID home,int requested){
        var roster=CarrierFleetData.get(p.serverLevel());var fleet=roster.fleet(home).stream().filter(e->!e.destroyed).toList();int page=Math.max(0,Math.min(requested,Math.max(0,(fleet.size()-1)/PAGE_SIZE)));var rows=new ArrayList<Row>();var c=roster.entry(home);rows.add(new Row(home,c==null?"Linked carrier":c.name,0));
        for(int i=page*PAGE_SIZE;i<Math.min(fleet.size(),(page+1)*PAGE_SIZE);i++){var e=fleet.get(i);rows.add(new Row(e.id,e.name,e.number));}
        final int pages=(fleet.size()+PAGE_SIZE-1)/PAGE_SIZE;
        p.openMenu(new SimpleMenuProvider((id,inv,who)->new CarrierFleetMenu(id,inv,p,home,page,pages,rows),Component.literal("Carrier Fleet")),buf->{buf.writeUUID(home);buf.writeVarInt(page);buf.writeVarInt(pages);buf.writeUtf(CarrierFleet.leaving(p),256);buf.writeVarInt(rows.size());for(var row:rows){buf.writeUUID(row.id);buf.writeUtf(row.name.length()>128?row.name.substring(0,128):row.name,128);buf.writeVarInt(row.number);}});
    }
    private void refresh(){
        if(owner==null)return;long now=owner.serverLevel().getGameTime();if(lastRefresh>=0&&now-lastRefresh<10)return;lastRefresh=now;
        var roster=CarrierFleetData.get(owner.serverLevel());
        for(int i=0;i<rows.size();i++){
            var record=roster.entry(rows.get(i).id);int status=CarrierFleet.status(owner,record),energy=0,capacity=0;var e=CarrierFleet.entity(owner,rows.get(i).id);
            if(e!=null){roster.refresh(e);if(e instanceof VatagEntity v){energy=v.getEnergy();capacity=v.getMaxEnergy();}else{energy=DroneBatterySystem.currentEnergy(e);var tag=e.getPersistentData();capacity=tag.contains("sbwdroneconfigMaxEnergy")?tag.getInt("sbwdroneconfigMaxEnergy"):AddonConfig.maxEnergy();}}
            data.set(i*5,status);data.set(i*5+1,energy&65535);data.set(i*5+2,energy>>>16);data.set(i*5+3,capacity&65535);data.set(i*5+4,capacity>>>16);
        }
    }
    public int state(int row){return data.get(row*5);}public int energy(int row){return(data.get(row*5+1)&65535)|data.get(row*5+2)<<16;}public int capacity(int row){return(data.get(row*5+3)&65535)|data.get(row*5+4)<<16;}
    @Override public void broadcastChanges(){refresh();super.broadcastChanges();}
    @Override public boolean stillValid(Player p){return p.isAlive()&&(p.level().isClientSide||home.equals(CarrierFleet.home(p.getMainHandItem())));}
    @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
    @Override public boolean clickMenuButton(Player p,int action){
        if(!(p instanceof ServerPlayer sp)||!stillValid(p))return false;
        if(action==0){CarrierFleet.request(sp,home);return true;}
        if(action==1){var e=CarrierFleet.entity(sp,home);if(e!=null&&CarrierFleet.access(sp,e)){if(e instanceof Dc01Entity c)c.openDocks(sp);else if(e instanceof Ndc01Entity n)n.openBays(sp);}else CarrierFleet.message(sp,"Carrier bays unavailable: check ownership, range and connection.");return true;}
        if(action==2||action==3){CarrierFleet.open(sp,page+(action==2?-1:1));return true;}
        if(action>=10&&action<10+rows.size()){CarrierFleet.request(sp,rows.get(action-10).id);return true;}return false;
    }
}
