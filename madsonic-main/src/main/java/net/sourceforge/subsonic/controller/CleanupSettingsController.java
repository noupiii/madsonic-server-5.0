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

import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.dao.AlbumDao;
import net.sourceforge.subsonic.dao.ArtistDao;
import net.sourceforge.subsonic.dao.MediaFileDao;
import net.sourceforge.subsonic.dao.MusicFolderDao;
import net.sourceforge.subsonic.dao.PlaylistDao;
import net.sourceforge.subsonic.dao.TranscodingDao;
import net.sourceforge.subsonic.domain.Playlist;
import net.sourceforge.subsonic.service.MediaScannerService;
import net.sourceforge.subsonic.service.PlaylistService;
import net.sourceforge.subsonic.service.SecurityService;
import net.sourceforge.subsonic.service.SettingsService;
import net.sourceforge.subsonic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the page used to administrate the set of internet radio/tv stations.
 *
 * @author Sindre Mehus
 */
public class CleanupSettingsController extends ParameterizableViewController {

    private static final Logger LOG = Logger.getLogger(CleanupSettingsController.class);	
	
    private SecurityService securityService;
    private MediaScannerService mediaScannerService;
    private ArtistDao artistDao;
    private AlbumDao albumDao;
    private MediaFileDao mediaFolderDao;
	private MusicFolderDao musicFolderDao;	
    private PlaylistDao playlistDao;
    private TranscodingDao transcodingDao;    
    private PlaylistService playlistService;
    private SettingsService settingsService;

    // protected ModelAndView playlistSettings(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Map<String, Object> map = new HashMap<String, Object>();
		// return null;
    // }
    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        
        String playlistExportFolderPath = settingsService.getPlaylistExportFolder();

        if (request.getParameter("FullCleanupNow") != null) {
        	
        	musicFolderDao.DisableAllMusicFolder();
        	Thread.sleep(200);
            mediaScannerService.scanLibrary();
        	Thread.sleep(200);
        	mediaFolderDao.markNonPresent(new Date());
            expunge();
        	Thread.sleep(200);
        	musicFolderDao.EnableAllMusicFolder();
        	Thread.sleep(200);
        	mediaScannerService.scanLibrary();        	
			map.put("done", true);
			map.put("reload", true);	
        }		

        if (request.getParameter("FullscanNow") != null) {
        	
        	musicFolderDao.DisableAllMusicFolder();
        	Thread.sleep(200);
            mediaScannerService.scanLibrary();
        	Thread.sleep(200);
        	mediaFolderDao.markNonPresent(new Date());
        	musicFolderDao.EnableAllMusicFolder();
        	Thread.sleep(200);
        	mediaScannerService.scanLibrary();        	
			map.put("done", true);
			map.put("reload", true);	
        }	        
        
        if (request.getParameter("scanNow") != null) {
            mediaScannerService.scanLibrary();
			map.put("done", true);
            map.put("reload", true);			
        }

        if (request.getParameter("resetPlaylists") != null) {
        	playlistDao.deleteAllImportedPlaylists();
            mediaScannerService.scanLibrary();
			map.put("done", true);
            map.put("reload", true);
        }        
		
        if (request.getParameter("deletePlaylists") != null) {
        	playlistDao.deleteAllPlaylists();
			map.put("done", true);
            map.put("reload", true);
        }        
        
        if (request.getParameter("cleanupHistory") != null) {
			mediaFolderDao.cleanupStatistics();			
			map.put("done", true);
            map.put("reload", true);			
        }        

        if (request.getParameter("reset2Subsonic") != null) {
        	transcodingDao.reset2Subsonic();
			map.put("done", true);
        }	        
        if (request.getParameter("reset2FLV") != null) {
        	transcodingDao.reset2FLV();
			map.put("done", true);
        }	        
        if (request.getParameter("reset2WEBM") != null) {
        	transcodingDao.reset2WEBM();
			map.put("done", true);
        }	        
        if (request.getParameter("reset2MadsonicFLV") != null) {
        	transcodingDao.reset2MadsonicFLV();
			map.put("done", true);
        }	        
        if (request.getParameter("reset2MadsonicWEBM") != null) {
        	transcodingDao.reset2MadsonicWEBM();
			map.put("done", true);
        }	        
        if (request.getParameter("reset2MadsonicMP4") != null) {
        	transcodingDao.reset2MadsonicMP4();
			map.put("done", true);
        }	        
        
        if (request.getParameter("expunge") != null) {
            expunge();
			map.put("done", true);
        }		
		
        if (request.getParameter("exportPlaylists") != null) {
			playlistService.exportAllPlaylists();
			map.put("done", true);
//			map.put("bug", true);
//			map.put("bugInfo", "your Bug )c:=");
	        }		
        
        if (request.getParameter("resetControl") != null) {
            securityService.resetControl();
			map.put("done", true);
            }		
        	map.put("exportfolder", playlistExportFolderPath);			
			map.put("scanning", mediaScannerService.isScanning());	
			
        ModelAndView result = super.handleRequestInternal(request, response);
        
		if (request.getRequestURI().toLowerCase().contains("/playlistSettings.view".toLowerCase())) {
//			LOG.warn("## FOUND " + request.getRequestURI().toLowerCase());
            result.setViewName("playlistSettings");
        }
        if (request.getRequestURI().toLowerCase().contains("/folderSettings.view".toLowerCase())) {
//    		LOG.warn("## FOUND " + request.getRequestURI().toLowerCase());
        	result.setViewName("folderSettings");
        }
        result.addObject("model", map);
        return result;
    }

    private void expunge() {
        artistDao.expunge();
        albumDao.expunge();
        mediaFolderDao.expunge();
    }	
	
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }    
 
     public void setMediaScannerService(MediaScannerService mediaScannerService) {
        this.mediaScannerService = mediaScannerService;
    }

     public void setTranscodingDao(TranscodingDao transcodingDao) {
         this.transcodingDao = transcodingDao;
     }
     
    public void setPlaylistDao(PlaylistDao playlistDao) {
        this.playlistDao = playlistDao;
    }
	
    public void setArtistDao(ArtistDao artistDao) {
        this.artistDao = artistDao;
    }

    public void setAlbumDao(AlbumDao albumDao) {
        this.albumDao = albumDao;
    }

    public void setMediaFolderDao(MediaFileDao mediaFolderDao) {
        this.mediaFolderDao = mediaFolderDao;
    }

    public void setMusicFolderDao(MusicFolderDao musicFolderDao) {
        this.musicFolderDao = musicFolderDao;
    }

    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }
    
    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }
}
