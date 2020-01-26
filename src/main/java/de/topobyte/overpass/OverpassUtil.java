// Copyright 2019 Sebastian Kuerten
//
// This file is part of overpass-utils.
//
// overpass-utils is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// overpass-utils is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with overpass-utils. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.overpass;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.utils.URIBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.adt.geo.BBox;

public class OverpassUtil
{

	final static Logger logger = LoggerFactory.getLogger(OverpassUtil.class);

	private static final String SCHEME = "http";
	private static final String ENDPOINT = "overpass-api.de/api/interpreter";

	private static URIBuilder builder()
	{
		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setScheme(SCHEME);
		uriBuilder.setHost(ENDPOINT);
		return uriBuilder;
	}

	/**
	 * Retrieves the query to the Overpass API for the specified bounding box.
	 */
	public static String query(BBox box)
	{
		URIBuilder uriBuilder = builder();
		String dataTemplate = "(node(%f,%f,%f,%f);<;>;);out;";
		String data = String.format(dataTemplate, box.getLat2(), box.getLon1(),
				box.getLat1(), box.getLon2());
		uriBuilder.addParameter("data", data);
		return uriBuilder.toString();
	}

	/**
	 * Retrieves the query to the Overpass API for the specified polygon.
	 */
	public static String query(Polygon polygon)
	{
		URIBuilder uriBuilder = builder();
		String dataTemplate = "(node(poly:\"%s\");<;>;);out;";
		String data = String.format(dataTemplate, polystring(polygon));
		uriBuilder.addParameter("data", data);
		return uriBuilder.toString();
	}

	private static String polystring(Polygon polygon)
	{
		StringBuilder strb = new StringBuilder();
		LinearRing exterior = (LinearRing) polygon.getExteriorRing();
		Coordinate c0 = exterior.getCoordinateN(0);
		strb.append(String.format("%.6f %.6f", c0.y, c0.x));
		for (int i = 1; i < exterior.getNumPoints() - 1; i++) {
			Coordinate c = exterior.getCoordinateN(i);
			strb.append(" ");
			strb.append(String.format("%.6f %.6f", c.y, c.x));
		}
		return strb.toString();
	}

	/**
	 * Retrieves the path to a cache file in /tmp that is used to cache data
	 * from the specified bounding box.
	 */
	public static Path cacheFile(BBox box)
	{
		String hashTemplate = "%f,%f,%f,%f";
		String hash = String.format(hashTemplate, box.getLat2(), box.getLon1(),
				box.getLat1(), box.getLon2());

		String sha1 = DigestUtils.sha1Hex(hash);

		String cacheFilename = String.format("%s.osm", sha1);
		Path pathTemp = Paths.get("/tmp");
		Path cacheFile = pathTemp.resolve(cacheFilename);
		return cacheFile;
	}

	/**
	 * Retrieves data from Overpass API for the specified bounding box and
	 * stores it in the file that {@link #cache(BBox)} returns. If the cache
	 * file already exists, nothing is downloaded.
	 */
	public static void cache(BBox box) throws MalformedURLException, IOException
	{
		cache(box, false);
	}

	/**
	 * Retrieves data from Overpass API for the specified bounding box and
	 * stores it in the file that {@link #cache(BBox)} returns. If the cache
	 * file already exists, nothing is downloaded.
	 * 
	 * @param forceRefresh
	 *            whether to delete the cache file if it already exists.
	 */
	public static void cache(BBox box, boolean forceRefresh)
			throws MalformedURLException, IOException
	{
		Path cacheFile = cacheFile(box);
		logger.debug("Cache file: " + cacheFile);

		if (Files.exists(cacheFile)) {
			if (forceRefresh) {
				Files.deleteIfExists(cacheFile);
				logger.debug("File exists, deleting");
			} else {
				logger.debug("File exists, keeping it");
				return;
			}
		}

		logger.debug("Downloading from Overpass...");

		String query = OverpassUtil.query(box);
		InputStream input = new URL(query).openStream();
		Files.copy(input, cacheFile);
	}

}
