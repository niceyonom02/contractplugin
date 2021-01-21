import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class EasyContract extends JavaPlugin implements Listener {
    public static EasyContract Main;
    private ContractManager contractManager;

    @Override
    public void onEnable(){
        Main = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        contractManager = new ContractManager();
        getCommand("수락").setExecutor(contractManager);
        getCommand("거절").setExecutor(contractManager);
    }

    public ContractManager getContractManager(){
        return contractManager;
    }

    @Override
    public void onDisable(){
        contractManager.save();
    }
}
