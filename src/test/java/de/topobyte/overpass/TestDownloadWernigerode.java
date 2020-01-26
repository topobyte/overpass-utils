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
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

public class TestDownloadWernigerode
{

	public static void main(String[] args)
			throws ParseException, MalformedURLException, IOException
	{
		InputStream wktInput = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("Wernigerode.wkt");
		Geometry polygon = new WKTReader()
				.read(new InputStreamReader(wktInput));

		DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(
				polygon);
		simplifier.setDistanceTolerance(0.0003);
		Geometry result = simplifier.getResultGeometry();

		String query = OverpassUtil.query((Polygon) result);
		InputStream input = new URL(query).openStream();
		Files.copy(input, Paths.get("/tmp/wernigerode.osm"));
	}

}
