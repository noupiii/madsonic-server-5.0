package net.sourceforge.subsonic.dao;

import java.util.Date;
import java.util.List;

import net.sourceforge.subsonic.domain.CoverArtScheme;
import net.sourceforge.subsonic.domain.Player;
import net.sourceforge.subsonic.domain.PlayerTechnology;
import net.sourceforge.subsonic.domain.PlayQueue;
import net.sourceforge.subsonic.domain.TranscodeScheme;

/**
 * Unit test of {@link PlayerDao}.
 *
 * @author Sindre Mehus
 */
public class PlayerDaoTestCase extends DaoTestCaseBase {

    @Override
    protected void setUp() throws Exception {
        getJdbcTemplate().execute("delete from player");
    }

    public void testCreatePlayer() {
        Player player = new Player();
        player.setName("name");
        player.setType("type");
        player.setUsername("username");
        player.setIpAddress("ipaddress");
        player.setDynamicIp(false);
        player.setAutoControlEnabled(false);
        player.setTechnology(PlayerTechnology.EXTERNAL_WITH_PLAYLIST);
        player.setClientId("android");
        player.setLastSeen(new Date());
        player.setCoverArtScheme(CoverArtScheme.LARGE);
        player.setTranscodeScheme(TranscodeScheme.MAX_160);

        playerDao.createPlayer(player);
        Player newPlayer = playerDao.getAllPlayers().get(0);
        assertPlayerEquals(player, newPlayer);

        Player newPlayer2 = playerDao.getPlayerById(newPlayer.getId());
        assertPlayerEquals(player, newPlayer2);
    }

    public void testDefaultValues() {
        playerDao.createPlayer(new Player());
        Player player = playerDao.getAllPlayers().get(0);

        assertTrue("Player should have dynamic IP by default.", player.isDynamicIp());
        assertTrue("Player should be auto-controlled by default.", player.isAutoControlEnabled());
        assertNull("Player client ID should be null by default.", player.getClientId());
    }

    public void testIdentity() {
        Player player = new Player();

        playerDao.createPlayer(player);
        assertEquals("Wrong ID", "1", player.getId());
        assertEquals("Wrong number of players.", 1, playerDao.getAllPlayers().size());

        playerDao.createPlayer(player);
        assertEquals("Wrong ID", "2", player.getId());
        assertEquals("Wrong number of players.", 2, playerDao.getAllPlayers().size());

        playerDao.createPlayer(player);
        assertEquals("Wrong ID", "3", player.getId());
        assertEquals("Wrong number of players.", 3, playerDao.getAllPlayers().size());

        playerDao.deletePlayer("3");
        playerDao.createPlayer(player);
        assertEquals("Wrong ID", "3", player.getId());
        assertEquals("Wrong number of players.", 3, playerDao.getAllPlayers().size());

        playerDao.deletePlayer("2");
        playerDao.createPlayer(player);
        assertEquals("Wrong ID", "4", player.getId());
        assertEquals("Wrong number of players.", 3, playerDao.getAllPlayers().size());
    }

    public void testPlaylist() {
        Player player = new Player();
        playerDao.createPlayer(player);
        PlayQueue playQueue = player.getPlayQueue();
        assertNotNull("Missing playlist.", playQueue);

        playerDao.deletePlayer(player.getId());
        playerDao.createPlayer(player);
        assertNotSame("Wrong playlist.", playQueue, player.getPlayQueue());
    }

    public void testGetPlayersForUserAndClientId() {
        Player player = new Player();
        player.setUsername("sindre");
        playerDao.createPlayer(player);
        player = playerDao.getAllPlayers().get(0);

        List<Player> players = playerDao.getPlayersForUserAndClientId("sindre", null);
        assertFalse("Error in getPlayersForUserAndClientId().", players.isEmpty());
        assertPlayerEquals(player, players.get(0));
        assertTrue("Error in getPlayersForUserAndClientId().", playerDao.getPlayersForUserAndClientId("sindre", "foo").isEmpty());

        player.setClientId("foo");
        playerDao.updatePlayer(player);

        players = playerDao.getPlayersForUserAndClientId("sindre", null);
        assertTrue("Error in getPlayersForUserAndClientId().", players.isEmpty());
        players = playerDao.getPlayersForUserAndClientId("sindre", "foo");
        assertFalse("Error in getPlayersForUserAndClientId().", players.isEmpty());
        assertPlayerEquals(player, players.get(0));
    }

    public void testUpdatePlayer() {
        Player player = new Player();
        playerDao.createPlayer(player);
        assertPlayerEquals(player, playerDao.getAllPlayers().get(0));

        player.setName("name");
        player.setType("Winamp");
        player.setTechnology(PlayerTechnology.WEB);
        player.setClientId("foo");
        player.setUsername("username");
        player.setIpAddress("ipaddress");
        player.setDynamicIp(true);
        player.setAutoControlEnabled(false);
        player.setLastSeen(new Date());
        player.setCoverArtScheme(CoverArtScheme.LARGE);
        player.setTranscodeScheme(TranscodeScheme.MAX_160);

        playerDao.updatePlayer(player);
        Player newPlayer = playerDao.getAllPlayers().get(0);
        assertPlayerEquals(player, newPlayer);
    }

    public void testDeletePlayer() {
        assertEquals("Wrong number of players.", 0, playerDao.getAllPlayers().size());

        playerDao.createPlayer(new Player());
        assertEquals("Wrong number of players.", 1, playerDao.getAllPlayers().size());

        playerDao.createPlayer(new Player());
        assertEquals("Wrong number of players.", 2, playerDao.getAllPlayers().size());

        playerDao.deletePlayer("1");
        assertEquals("Wrong number of players.", 1, playerDao.getAllPlayers().size());

        playerDao.deletePlayer("2");
        assertEquals("Wrong number of players.", 0, playerDao.getAllPlayers().size());
    }

    private void assertPlayerEquals(Player expected, Player actual) {
        assertEquals("Wrong ID.", expected.getId(), actual.getId());
        assertEquals("Wrong name.", expected.getName(), actual.getName());
        assertEquals("Wrong technology.", expected.getTechnology(), actual.getTechnology());
        assertEquals("Wrong client ID.", expected.getClientId(), actual.getClientId());
        assertEquals("Wrong type.", expected.getType(), actual.getType());
        assertEquals("Wrong username.", expected.getUsername(), actual.getUsername());
        assertEquals("Wrong IP address.", expected.getIpAddress(), actual.getIpAddress());
        assertEquals("Wrong dynamic IP.", expected.isDynamicIp(), actual.isDynamicIp());
        assertEquals("Wrong auto control enabled.", expected.isAutoControlEnabled(), actual.isAutoControlEnabled());
        assertEquals("Wrong last seen.", expected.getLastSeen(), actual.getLastSeen());
        assertEquals("Wrong cover art scheme.", expected.getCoverArtScheme(), actual.getCoverArtScheme());
        assertEquals("Wrong transcode scheme.", expected.getTranscodeScheme(), actual.getTranscodeScheme());
    }
}