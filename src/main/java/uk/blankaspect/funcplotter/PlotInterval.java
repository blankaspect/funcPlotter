/*====================================================================*\

PlotInterval.java

Plot interval class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.math.BigDecimal;

import uk.blankaspect.common.range.IntegerRange;

//----------------------------------------------------------------------


// PLOT INTERVAL CLASS


class PlotInterval
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		double	MIN_VALUE	= -1.0E100;
	public static final		double	MAX_VALUE	= -MIN_VALUE;

	public static final		int	MAX_NUM_SIGNIFICANT_DIGITS	= 12;

	private static final	String	DEFAULT_LOWER_EP_STR	= "-1.0";
	private static final	String	DEFAULT_UPPER_EP_STR	= "1.0";

	private static final	double	RECIP_LOG_10	= 1.0 / Math.log(10.0);

	private static final	String	FIXED_ZERO_STR				= "0.0";
	private static final	String	FIXED_MINUS_ZERO_STR		= "-0.0";
	private static final	String	SCIENTIFIC_ZERO_STR			= "0.0E0";
	private static final	String	SCIENTIFIC_MINUS_ZERO_STR	= "-0.0E0";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public PlotInterval()
	{
		this(DEFAULT_LOWER_EP_STR, DEFAULT_UPPER_EP_STR);
	}

	//------------------------------------------------------------------

	public PlotInterval(double lowerEndpoint,
						double upperEndpoint)
	{
		this(Double.toString(lowerEndpoint), Double.toString(upperEndpoint));
	}

	//------------------------------------------------------------------

	public PlotInterval(BigDecimal lowerEndpoint,
						BigDecimal upperEndpoint)
	{
		this.lowerEndpoint = lowerEndpoint;
		this.upperEndpoint = upperEndpoint;
		this.lowerEndpoint = new BigDecimal(getLowerEndpointString());
		this.upperEndpoint = new BigDecimal(getUpperEndpointString());
	}

	//------------------------------------------------------------------

	/**
	 * @throws NumberFormatException
	 */

	public PlotInterval(String lowerEndpointStr,
						String upperEndpointStr)
	{
		this(new BigDecimal(lowerEndpointStr), new BigDecimal(upperEndpointStr));
	}

	//------------------------------------------------------------------

	public PlotInterval(PlotInterval interval)
	{
		this(interval.lowerEndpoint, interval.upperEndpoint);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static boolean hasTooManySignificantDigits(String str)
	{
		str = str.toUpperCase();
		int endIndex = str.indexOf('E');
		if (endIndex < 0)
			endIndex = str.length();
		int pointIndex = str.indexOf('.');
		if ((pointIndex >= 0) && (pointIndex < endIndex))
		{
			int i = pointIndex;
			while (++i < endIndex)
			{
				if (str.charAt(i) != '0')
					break;
			}
			if (i == endIndex)
				endIndex = pointIndex;
		}
		int numDigits = 0;
		for (int i = 0; i < endIndex; i++)
		{
			char ch = str.charAt(i);
			if ((ch == '0') && (numDigits == 0))
				continue;
			if ((ch >= '0') && (ch <= '9'))
				++numDigits;
		}
		return (numDigits > MAX_NUM_SIGNIFICANT_DIGITS);
	}

	//------------------------------------------------------------------

	public static String doubleToString(double       value,
										int          numFractionDigits,
										IntegerRange exponentRange,
										boolean      applyFixedExponent,
										int          fixedExponent)
	{
		final	int	EXPONENT_OFFSET	= MAX_NUM_SIGNIFICANT_DIGITS + 1;

		// Handle zero as special case
		if (value == 0.0)
			return FIXED_ZERO_STR;

		// Set flag for negative value; make value absolute
		boolean negative = (value < 0.0);
		if (negative)
			value = -value;

		// Get significand as string
		int exponent = (int)Math.floor(Math.log(value) * RECIP_LOG_10);
		String sigStr = Long.toString((long)(value * Math.pow(10.0, EXPONENT_OFFSET - exponent)));

		// Normalise radix point and exponent
		exponent += sigStr.length() - EXPONENT_OFFSET;
		int pointIndex = 0;
		while (pointIndex < sigStr.length())
		{
			--exponent;
			if (sigStr.charAt(pointIndex++) != '0')
				break;
		}

		// Apply fixed exponent
		if (applyFixedExponent)
		{
			pointIndex += exponent - fixedExponent;
			exponent = fixedExponent;
		}

		// Remove exponent if it is within fixed-point range
		if (exponentRange.contains(exponent))
		{
			pointIndex += exponent;
			exponent = 0;
		}

		// Create string of digits in buffer, padding with zeros as required
		StringBuilder buffer = new StringBuilder(MAX_NUM_SIGNIFICANT_DIGITS << 1);
		if (pointIndex < 0)
		{
			buffer.append("0".repeat(-pointIndex));
			pointIndex = 0;
		}
		buffer.append(sigStr);
		int maxLength = Math.min(pointIndex + numFractionDigits, buffer.length() - 2);
		if (pointIndex > buffer.length())
			buffer.append("0".repeat(pointIndex - buffer.length()));

		// Round up value and remove extraneous digits
		int i = maxLength;
		if (buffer.charAt(i) >= '5')
		{
			while (--i >= 0)
			{
				char ch = buffer.charAt(i);
				if (ch == '9')
					buffer.setCharAt(i, '0');
				else
				{
					buffer.setCharAt(i, ++ch);
					break;
				}
			}
			if (i < 0)
			{
				buffer.insert(0, '1');
				if (applyFixedExponent)
					++pointIndex;
				else
				{
					if (exponentRange.contains(++exponent))
					{
						pointIndex += exponent;
						exponent = 0;
					}
				}
			}
		}
		buffer.setLength(maxLength);

		// Insert radix point
		if (pointIndex < 0)
		{
			buffer.insert(0, "0".repeat(-pointIndex));
			pointIndex = 0;
		}
		if (pointIndex > buffer.length())
			buffer.append("0".repeat(pointIndex - buffer.length()));
		buffer.insert(pointIndex, '.');

		// Strip leading zeros
		i = 0;
		while (buffer.charAt(i) == '0')
			++i;
		if (buffer.charAt(i) == '.')
		{
			if (i == 0)
				buffer.insert(0, '0');
			else
				--i;
		}
		buffer.delete(0, i);

		// Strip trailing zeros
		i = buffer.length();
		while (buffer.charAt(--i) == '0')
		{
			// do nothing
		}
		buffer.setLength(++i);
		if (buffer.charAt(i - 1) == '.')
			buffer.append('0');

		// Insert minus sign at front of negative value
		if (negative)
			buffer.insert(0, '-');

		// Append exponent
		if (exponent != 0)
		{
			buffer.append('E');
			buffer.append(Integer.toString(exponent));
		}

		// Return string
		String str = buffer.toString();
		if (str.equals(FIXED_MINUS_ZERO_STR) || str.equals(SCIENTIFIC_ZERO_STR) ||
			 str.equals(SCIENTIFIC_MINUS_ZERO_STR))
			str = FIXED_ZERO_STR;
		return str;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public String toString()
	{
		return new String(lowerEndpoint + ", " + upperEndpoint);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public double getLowerEndpoint()
	{
		return lowerEndpoint.doubleValue();
	}

	//------------------------------------------------------------------

	public double getUpperEndpoint()
	{
		return upperEndpoint.doubleValue();
	}

	//------------------------------------------------------------------

	public String getLowerEndpointString()
	{
		return coordToString(getLowerEndpoint());
	}

	//------------------------------------------------------------------

	public String getUpperEndpointString()
	{
		return coordToString(getUpperEndpoint());
	}

	//------------------------------------------------------------------

	public double getInterval()
	{
		return upperEndpoint.doubleValue() - lowerEndpoint.doubleValue();
	}

	//------------------------------------------------------------------

	public double getHalfInterval()
	{
		return 0.5 * (upperEndpoint.doubleValue() - lowerEndpoint.doubleValue());
	}

	//------------------------------------------------------------------

	public double getMedian()
	{
		return 0.5 * (upperEndpoint.doubleValue() + lowerEndpoint.doubleValue());
	}

	//------------------------------------------------------------------

	public boolean isValid()
	{
		double dLowerEndpoint = getLowerEndpoint();
		double dUpperEndpoint = getUpperEndpoint();
		return (dLowerEndpoint >= MIN_VALUE) && (dLowerEndpoint <= MAX_VALUE)
				&& (dUpperEndpoint >= MIN_VALUE) && (dUpperEndpoint <= MAX_VALUE)
				&& (dLowerEndpoint < dUpperEndpoint);
	}

	//------------------------------------------------------------------

	public boolean equals(PlotInterval interval)
	{
		return (interval != null) && lowerEndpoint.equals(interval.lowerEndpoint)
				&& upperEndpoint.equals(interval.upperEndpoint);
	}

	//------------------------------------------------------------------

	public String coordToString(double value)
	{
		AppConfig config = AppConfig.INSTANCE;
		return doubleToString(value, config.getNumFractionDigits(),
							  config.getFixedPointExponentRange(),
							  !config.isNormaliseScientificNotation(), getExponent());
	}

	//------------------------------------------------------------------

	public int getExponent()
	{
		double absEp = Math.min(Math.abs(getLowerEndpoint()), Math.abs(getUpperEndpoint()));
		int exponent = (absEp == 0.0) ? 0 : (int)Math.floor(Math.log(absEp) * RECIP_LOG_10);
		if (absEp * Math.pow(10.0, -exponent) >= 10.0)
			++exponent;
		return exponent;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	BigDecimal	lowerEndpoint;
	private	BigDecimal	upperEndpoint;

}

//----------------------------------------------------------------------
