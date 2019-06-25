from __future__ import division

def find_intersection( p0, p1, p2, p3 ) :

    s10_x = p1[0] - p0[0]
    s10_y = p1[1] - p0[1]
    s32_x = p3[0] - p2[0]
    s32_y = p3[1] - p2[1]

    denom = s10_x * s32_y - s32_x * s10_y

    if denom == 0 : return None # collinear

    denom_is_positive = denom > 0

    s02_x = p0[0] - p2[0]
    s02_y = p0[1] - p2[1]

    s_numer = s10_x * s02_y - s10_y * s02_x

    if (s_numer < 0) == denom_is_positive : return None # no collision

    t_numer = s32_x * s02_y - s32_y * s02_x

    if (t_numer < 0) == denom_is_positive : return None # no collision

    if (s_numer > denom) == denom_is_positive or (t_numer > denom) == denom_is_positive : return None # no collision


    # collision detected

    t = t_numer / denom

    intersection_point = [ p0[0] + (t * s10_x), p0[1] + (t * s10_y) ]
    return intersection_point

# Check all lines for intersections
def intersections( hlines , vlines ):
    intersections = set()
    for test_segment in vlines:
        for line_segment in hlines:
            #p0[0],p0[1], p1[0],p1[1] = test_segment[0][0], test_segment[0][1],test_segment[0][2], test_segment[0][3]
            #p2[0],p2[1], p3[0],p3[1] = line_segment[0][0], line_segment[0][1],line_segment[0][2], line_segment[0][3]
            result = find_intersection([test_segment[0][0], test_segment[0][1]], [test_segment[0][2], test_segment[0][3]], [line_segment[0][0], line_segment[0][1]],[line_segment[0][2], line_segment[0][3]])
            if result is not None:
                intersections.add(tuple(result))

    return (intersections)