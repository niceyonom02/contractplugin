import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ContractManager implements Listener, AfterContractProcessor, CommandExecutor {
    public ContractManager(){
        Bukkit.getPluginManager().registerEvents(this, EasyContract.Main);
    }
    private HashMap<UUID, UUID> requestQueue = new HashMap<UUID, UUID>();
    private ArrayList<Contract> contractList = new ArrayList<Contract>();

    @EventHandler
    public void onClickPlayer(PlayerInteractEntityEvent e){
        if(e.getRightClicked() instanceof Player){
            Player from = e.getPlayer();
            Player to = (Player) e.getRightClicked();

            if(requestQueue.get(from.getUniqueId()) == to.getUniqueId()){
                from.sendMessage("이미 요청을 보낸 상대입니다!");
                return;
            }

            requestQueue.put(from.getUniqueId(), to.getUniqueId());
            from.sendMessage(to.getName() + "님에게 거래 요청을 보냈습니다!");
            to.sendMessage(from.getName() + "님의 거래 요청이 도착하였습니다! /수락 | 거절 " + from.getName());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        if(requestQueue.containsKey(e.getPlayer().getUniqueId())) requestQueue.remove(requestQueue.get(e.getPlayer().getUniqueId()));
    }

    public boolean acceptContract(UUID from, UUID to){
        if(requestQueue.get(from) == to){
            requestQueue.remove(from);
            createContract(from, to);
            return true;
        } else{
            return false;
        }
    }

    public void save(){
        int currentSize = contractList.size();
        for(int i = 0; i < currentSize; i++){
            Contract con = contractList.get(0);
            con.finish();
        }
    }

    public boolean rejectContract(UUID from, UUID to){
        if(requestQueue.get(from) == to){
            if(Bukkit.getPlayer(from) != null){
                Bukkit.getPlayer(from).sendMessage(Bukkit.getPlayer(to) + "님이 거래를 거절하셨습니다!");
            }
            Bukkit.getPlayer(to).sendMessage("거래를 거절하였습니다!");
            requestQueue.remove(from);
            return true;
        } else{
            return false;
        }
    }

    public void createContract(UUID from, UUID to){
        Contract contract = new Contract(from, to, this);
        contractList.add(contract);
    }

    public void finishContract(Contract contract) {
        contractList.remove(contract);
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)) return false;

        Player player = (Player) commandSender;
        if(s.equalsIgnoreCase("수락")){
            if(strings.length >= 1){
                if(Bukkit.getPlayer(strings[0]) != null){
                    if(!acceptContract(Bukkit.getPlayer(strings[0]).getUniqueId(), player.getUniqueId())){
                        player.sendMessage("해당 플레이어에게서 온 거래 요청이 없습니다!");
                        return false;
                    }
                } else{
                    player.sendMessage("해당 플레이어는 오프라인 상태입니다!");
                    return false;
                }
            } else{
                player.sendMessage("/수락 [닉네임]");
            }
        } else if(s.equalsIgnoreCase("거절")){
            if(strings.length >= 1){
                if(Bukkit.getPlayer(strings[0]) != null){
                    if(!rejectContract(Bukkit.getPlayer(strings[0]).getUniqueId(), player.getUniqueId())){
                        player.sendMessage("해당 플레이어에게서 온 거래 요청이 없습니다!");
                        return false;
                    }
                } else{
                    player.sendMessage("해당 플레이어는 오프라인 상태입니다!");
                    return false;
                }
            } else{
                player.sendMessage("/거절 [닉네임]");
            }
        }
        return false;
    }
}
