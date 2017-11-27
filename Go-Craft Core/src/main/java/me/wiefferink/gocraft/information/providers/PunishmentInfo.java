package me.wiefferink.gocraft.information.providers;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.BmAPI;
import me.confuser.banmanager.data.PlayerData;
import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.interactivemessenger.processing.Message;

import java.sql.SQLException;

public class PunishmentInfo extends InformationProvider {


    @Override
    public void showAsync(InformationRequest request) {
        BanManager manager = plugin.getBanManagerLink().get();
        try {
            PlayerData playerData = BmAPI.getPlayer(request.getAboutOffline().getUniqueId());
            request.message(Message.fromKey("information-punishments").replacements(manager.getPlayerBanRecordStorage().getCount(playerData),
                    manager.getPlayerKickStorage().getCount(playerData),
                    manager.getPlayerMuteRecordStorage().getCount(playerData)));
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
