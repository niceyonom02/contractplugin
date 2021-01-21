import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class Contract implements Listener {
    public static List<Integer> player1Slot = Arrays.asList(10, 11, 12, 19, 20, 21, 28, 29, 30);
    public static List<Integer> player2Slot = Arrays.asList(14, 15, 16, 23, 24, 25, 32, 33, 34);
    public static List<Integer> notGlassslot = Arrays.asList(46, 47, 48, 50, 51, 52);
    private Player player1;
    private Player player2;
    private final AfterContractProcessor processor;
    private final Inventory contractInventory;
    private final String title;
    private Boolean isContractCancelled = false;
    private Boolean isPlayer1Ready = false;
    private Boolean isPlayer2Ready = false;

    public Contract(UUID from, UUID to, AfterContractProcessor processor){
        this.player1 = Bukkit.getPlayer(from);
        this.player2 = Bukkit.getPlayer(to);
        this.processor = processor;
        title = player1.getName() + " === " + player2.getName();

        Bukkit.getPluginManager().registerEvents(this, EasyContract.Main);

        contractInventory = Bukkit.createInventory(null, 54, title);

        setItem();
        startContract();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        if(e.getPlayer().getUniqueId() == player1.getUniqueId() || e.getPlayer().getUniqueId() == player2.getUniqueId()){
            cancelContract();
        }
    }

    public void setItem() {
        ItemStack glass = new ItemStack(Material.GLASS);
        for(int slotNumber = 0; slotNumber < 54; slotNumber ++){
            if(isDecorationSlot(slotNumber)){
                contractInventory.setItem(slotNumber, glass);
            }
        }

        ItemStack greenGlass = new ItemStack(Material.STAINED_GLASS, 1, (short) 14);
        contractInventory.setItem(47, greenGlass.clone());
        contractInventory.setItem(51, greenGlass.clone());
    }

    private boolean isDecorationSlot(int slotNumber){
        return !(player1Slot.contains(slotNumber) || player2Slot.contains(slotNumber) || notGlassslot.contains(slotNumber));
    }

    public void startContract(){
        //player1.closeInventory();
       // player2.closeInventory();

        player1.sendMessage(player2 + "님과의 거래를 시작합니다.");
        player2.sendMessage(player1 + "님과의 거래를 시작합니다.");

        player1.openInventory(contractInventory);
        player2.openInventory(contractInventory);

        Bukkit.getLogger().info("start");
        for(HumanEntity h : contractInventory.getViewers()){
            Bukkit.getLogger().info(h.getName());
        }
    }

    public void cancelContract(){
        isContractCancelled = true;
        player1.sendMessage(player2.getName() + "님과의 거래가 취소되었습니다");
        player2.sendMessage(player1.getName() + "님과의 거래가 취소되었습니다");

        givePlayer1Item(player1);
        givePlayer2Item(player2);

        clearUp();
    }

    public void clearUp(){
        Bukkit.getLogger().info("end");
        for(HumanEntity h : contractInventory.getViewers()){
            Bukkit.getLogger().info(h.getName());
        }

        contractInventory.clear();
        for(int i = 0; i < contractInventory.getViewers().size(); i++){
            contractInventory.getViewers().get(i).closeInventory();
        }

        for(int i = 0; i < contractInventory.getViewers().size(); i++){
            contractInventory.getViewers().get(i).closeInventory();
        }

        player1 = null;
        player2 = null;


        InventoryClickEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);


        processor.finishContract(this);
    }


    //플레이어 인벤토리는 클릭되게 해야함
    @EventHandler
    public void onInventoryClicked(InventoryClickEvent e){
        Bukkit.getLogger().info(e.getRawSlot() + "");
        if(e.getInventory().getTitle().equals(title)){
            if(e.getRawSlot() > 53) return;

            Player player = (Player) e.getWhoClicked();
            if(player.getUniqueId().equals(player1.getUniqueId())){
                if(!player1Slot.contains(e.getRawSlot())){
                    e.setCancelled(true);
                    if(e.getRawSlot() == 47){
                        togglePlayer1();
                    }
                }
            } else if(player.getUniqueId().equals(player2.getUniqueId())){
                if(!player2Slot.contains(e.getRawSlot())){
                    e.setCancelled(true);
                    if(e.getRawSlot() == 51){
                        togglePlayer2();
                    }
                }
            }
        }
    }

    public void togglePlayer1(){
        isPlayer1Ready = !isPlayer1Ready;
        if(isPlayer1Ready){
            ItemStack ready = new ItemStack(Material.STAINED_GLASS, 1, (short) 5);
            contractInventory.setItem(47, ready);
        } else{
            ItemStack unReady = new ItemStack(Material.STAINED_GLASS, 1, (short) 14);
            contractInventory.setItem(47, unReady);
        }
        checkCondition();
    }

    public void togglePlayer2(){

        isPlayer2Ready = !isPlayer2Ready;
        if(isPlayer2Ready){
            ItemStack ready = new ItemStack(Material.STAINED_GLASS, 1, (short) 5);
            contractInventory.setItem(51, ready);
        } else{
            ItemStack unReady = new ItemStack(Material.STAINED_GLASS, 1, (short) 14);
            contractInventory.setItem(51, unReady);
        }
        checkCondition();
    }

    public void checkCondition(){
        Bukkit.getLogger().info("check");
        for(HumanEntity h : contractInventory.getViewers()){
            Bukkit.getLogger().info(h.getName());
        }
        if(isPlayer1Ready && isPlayer2Ready){
            completeContraction();
        }
    }

    public void completeContraction(){
        isContractCancelled = true;
       givePlayer1Item(player2);
       givePlayer2Item(player1);

       player1.sendMessage("거래 종료");
       player2.sendMessage("거래 종료");

       clearUp();
    }

    @EventHandler
    public void CloseInventoryEvent(InventoryCloseEvent e){
        if(contractInventory.getTitle().equals(title)){
            if(!isContractCancelled){
                cancelContract();
            }
        }
    }

    public void finish(){
        cancelContract();
    }

    public void givePlayer1Item(Player to){
        ArrayList<ItemStack> giveList = new ArrayList<ItemStack>();
        for(int slot : player1Slot){
            if(contractInventory.getItem(slot) != null) {
                if(contractInventory.getItem(slot).getType() != Material.AIR){
                    giveList.add(contractInventory.getItem(slot));
                }
            }
        }

        for (ItemStack item : giveList) {
            to.getInventory().addItem(item);
        }

        giveList.clear();
    }

    public void givePlayer2Item(Player to){
        ArrayList<ItemStack> giveList = new ArrayList<ItemStack>();
        for(int slot : player2Slot){
            if(contractInventory.getItem(slot) != null) {
                if(contractInventory.getItem(slot).getType() != Material.AIR){
                    giveList.add(contractInventory.getItem(slot));
                }
            }
        }

        for (ItemStack item : giveList) {
            to.getInventory().addItem(item);
        }

        giveList.clear();
    }
}
