/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2009 (C) Sindre Mehus
 */
package net.sourceforge.subsonic.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import net.sourceforge.subsonic.command.SearchCommand;
import net.sourceforge.subsonic.domain.SearchCriteria;
import net.sourceforge.subsonic.domain.SearchResult;
import net.sourceforge.subsonic.domain.User;
import net.sourceforge.subsonic.domain.UserSettings;
import net.sourceforge.subsonic.service.PlayerService;
import net.sourceforge.subsonic.service.SecurityService;
import net.sourceforge.subsonic.service.SettingsService;
import net.sourceforge.subsonic.service.SearchService;

/**
 * Controller for the search page.
 *
 * @author Sindre Mehus
 */
public class SearchController extends SimpleFormController {

    private static final int MATCH_COUNT = 100;

    private SecurityService securityService;
    private SettingsService settingsService;
    private PlayerService playerService;
    private SearchService searchService;

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new SearchCommand();
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object com, BindException errors)
            throws Exception {
        SearchCommand command = (SearchCommand) com;

        User user = securityService.getCurrentUser(request);
        UserSettings userSettings = settingsService.getUserSettings(user.getUsername());
        
        int userGroupId = securityService.getCurrentUserGroupId(request); 
        
        command.setUser(user);
        command.setPartyModeEnabled(userSettings.isPartyModeEnabled());

        String any = StringUtils.trimToNull(command.getQuery());

        if (any != null) {

            SearchCriteria criteria = new SearchCriteria();
            criteria.setCount(MATCH_COUNT);
            criteria.setQuery(any);

            SearchResult artists = searchService.search(criteria, SearchService.IndexType.ARTIST, userGroupId);
            command.setArtists(artists.getMediaFiles());

            SearchResult albums = searchService.search(criteria, SearchService.IndexType.ALBUM, userGroupId);
            command.setAlbums(albums.getMediaFiles());

            SearchResult songs = searchService.search(criteria, SearchService.IndexType.SONG, userGroupId);
            command.setSongs(songs.getMediaFiles());

            command.setPlayer(playerService.getPlayer(request, response));
        }

        return new ModelAndView(getSuccessView(), errors.getModel());
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }
}
