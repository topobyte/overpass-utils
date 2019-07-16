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
import java.nio.file.Paths;

import de.topobyte.adt.geo.BBox;

public class TestDownloadBbox
{

	public static void main(String[] args)
			throws MalformedURLException, IOException
	{
		BBox bbox = new BBox(10.573852, 51.887041, 10.900494, 51.752181);

		String query = OverpassUtil.query(bbox);
		InputStream input = new URL(query).openStream();
		Files.copy(input, Paths.get("/tmp/bbox.osm"));
	}

}
