//
// Copyright (c) 2012 Mirko Nasato
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
// OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//
package conceptopedia;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {

	public static void main(String[] args) {
		try {
			ActionRequest request = new ActionRequest();
			Pattern parameter = Pattern.compile("--([a-z-]+)");

			int l = args.length;
			for (int i = 0; i < l; i++) {
				Matcher matcher = parameter.matcher(args[i]);
				if (matcher.find()) {
					
					Integer nbParameters = ActionRequest.ActionNbParameters.get(matcher.group(1));
					request.setAction(matcher.group(1));
					while(nbParameters > 0){
						i++;
						request.setParameter(args[i]);
						nbParameters--;
					}
				} else
					throw new Exception("Missing argument qualifier, please see manual");
			}

			request.call();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}